#!/usr/bin/env python3
"""Rename a project created from the MieHexTemplate-Starter GitHub template.

The starter repo is MieHexTemplate rendered with its default answers. This script swaps the default
mod id / package / class name / display name (and optionally the GitHub owner/repo) for your own,
in file contents *and* in file/directory names.

    python3 rename-mod.py --modid mymod --package io.github.me.mymod --name "My Mod" \
        [--classname MyMod] [--github-user me] [--github-repo mymod] [--dry-run]

Run it once, from the repository root, on a clean checkout (commit first so you can review the diff).
Uses only the Python standard library.
"""
from __future__ import annotations

import argparse
import keyword
import os
import re
import shutil
import sys
from pathlib import Path

# ---- values the starter repo was rendered with (kept in sync by the publish workflow) ----
OLD = {
    "modid": "miehexaddon",
    "package": "io.github.xm1221.miehexaddon",
    "classname": "MieHexAddon",
    "name": "MieHex Addon",
    "github_user": "xm1221",
    "github_repo": "miehexaddon",
}

SKIP_DIRS = {".git", ".gradle", "build", "run", ".venv", "node_modules", "_site", ".idea", "out", "__pycache__"}
SELF = Path(__file__).resolve()

MODID_RE = re.compile(r"^[a-z][a-z0-9_]{1,63}$")
IDENT_RE = re.compile(r"^[A-Za-z_][A-Za-z0-9_]*$")
JAVA_KEYWORDS = {
    "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue",
    "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if",
    "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private",
    "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
    "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false", "null", "_",
}
KOTLIN_HARD_KEYWORDS = {"as", "break", "class", "continue", "do", "else", "false", "for", "fun", "if", "in",
                        "interface", "is", "null", "object", "package", "return", "super", "this", "throw",
                        "true", "try", "typealias", "typeof", "val", "var", "when", "while"}


def die(msg: str) -> None:
    print(f"rename-mod: {msg}", file=sys.stderr)
    sys.exit(2)


def kotlin_package(pkg: str) -> str:
    return ".".join(f"`{p}`" if p in KOTLIN_HARD_KEYWORDS else p for p in pkg.split("."))


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--modid", required=True)
    ap.add_argument("--package", required=True, help="base Java package, e.g. io.github.me.mymod")
    ap.add_argument("--name", required=True, help="display name, e.g. 'My Mod'")
    ap.add_argument("--classname", help="class name base (default: --modid capitalized, underscores removed)")
    ap.add_argument("--github-user", default=None)
    ap.add_argument("--github-repo", default=None)
    ap.add_argument("--dry-run", action="store_true")
    ap.add_argument("--root", default=".", help="project root (default: current directory)")
    a = ap.parse_args()

    root = Path(a.root).resolve()
    if not (root / "settings.gradle.kts").is_file() or not (root / "gradle" / "libs.versions.toml").is_file():
        die(f"{root} does not look like the starter project root")

    new = {
        "modid": a.modid,
        "package": a.package,
        "classname": a.classname or "".join(p[:1].upper() + p[1:] for p in a.modid.split("_")),
        "name": a.name,
        "github_user": a.github_user or OLD["github_user"],
        "github_repo": a.github_repo or (a.modid if a.github_user else OLD["github_repo"]),
    }
    if not MODID_RE.match(new["modid"]):
        die("--modid must match ^[a-z][a-z0-9_]{1,63}$")
    parts = new["package"].split(".")
    if not all(IDENT_RE.match(p) and p not in JAVA_KEYWORDS for p in parts):
        die("--package must be a valid Java package (no keywords, dot separated identifiers)")
    if not IDENT_RE.match(new["classname"]) or keyword.iskeyword(new["classname"]):
        die("--classname must be a valid identifier")
    if '"' in new["name"] or "\\" in new["name"]:
        die("--name must not contain quotes or backslashes")

    # rename-mod already ran?
    probe = (root / "gradle.properties").read_text(encoding="utf-8")
    if f"modId={OLD['modid']}" not in probe:
        die(f"gradle.properties has no modId={OLD['modid']}; was the project already renamed?")

    old_pkg_path, new_pkg_path = OLD["package"].replace(".", "/"), new["package"].replace(".", "/")

    # ordered: most specific first
    replacements = [
        (f"github.com/{OLD['github_user']}/{OLD['github_repo']}", f"github.com/{new['github_user']}/{new['github_repo']}"),
        (f"{OLD['github_user'].lower()}.github.io/{OLD['github_repo']}", f"{new['github_user'].lower()}.github.io/{new['github_repo']}"),
        (kotlin_package(OLD["package"]), kotlin_package(new["package"])),
        (OLD["package"], new["package"]),
        (old_pkg_path, new_pkg_path),
        (OLD["name"], new["name"]),
        (OLD["classname"], new["classname"]),
        (OLD["classname"].lower() + "ModDependencies", new["classname"].lower() + "ModDependencies"),
        (OLD["modid"], new["modid"]),
    ]
    if a.github_user:
        replacements.append((f"github_user: {OLD['github_user']}", f"github_user: {new['github_user']}"))
        replacements.append((f'"{OLD["github_user"]}"', f'"{new["github_user"]}"'))  # fabric.mod.json authors
        replacements.append((f'authors = "{OLD["github_user"]}"', f'authors = "{new["github_user"]}"'))

    def walk():
        for dirpath, dirnames, filenames in os.walk(root):
            dirnames[:] = [d for d in dirnames if d not in SKIP_DIRS]
            for f in filenames:
                yield Path(dirpath) / f

    changed = 0
    for path in walk():
        if path.resolve() == SELF:
            continue
        try:
            text = path.read_text(encoding="utf-8")
        except (UnicodeDecodeError, OSError):
            continue  # binary
        new_text = text
        for o, n in replacements:
            new_text = new_text.replace(o, n)
        if new_text != text:
            changed += 1
            print(f"edit   {path.relative_to(root)}")
            if not a.dry_run:
                path.write_text(new_text, encoding="utf-8")

    # move package directories (src/*/java|kotlin/<old/pkg> -> <new/pkg>)
    moved = 0
    for src_root in sorted(root.glob("*/src/*/*")):
        old_dir = src_root / old_pkg_path
        if src_root.name in ("java", "kotlin") and old_dir.is_dir() and old_pkg_path != new_pkg_path:
            target = src_root / new_pkg_path
            print(f"move   {old_dir.relative_to(root)} -> {target.relative_to(root)}")
            moved += 1
            if not a.dry_run:
                tmp = src_root / ".rename-mod-tmp"
                shutil.move(str(old_dir), str(tmp))
                parent = old_dir.parent
                while parent != src_root and parent.is_dir() and not any(parent.iterdir()):
                    parent.rmdir()
                    parent = parent.parent
                target.parent.mkdir(parents=True, exist_ok=True)
                shutil.move(str(tmp), str(target))

    # rename remaining files/dirs whose names contain the mod id or class name (deepest first)
    paths = sorted((p for p in root.rglob("*") if not (set(p.relative_to(root).parts) & SKIP_DIRS)),
                   key=lambda p: len(p.parts), reverse=True)
    for p in paths:
        name = p.name
        new_name = name.replace(OLD["classname"], new["classname"]).replace(OLD["modid"], new["modid"])
        if new_name != name:
            print(f"rename {p.relative_to(root)} -> {new_name}")
            moved += 1
            if not a.dry_run and p.exists():
                p.rename(p.with_name(new_name))

    print(f"rename-mod: {changed} file(s) edited, {moved} path(s) moved/renamed"
          + (" (dry run, nothing written)" if a.dry_run else ""))
    if not a.dry_run:
        print("Next: review `git status` / `git diff`, then ./gradlew build. You can delete rename-mod.py afterwards.")


if __name__ == "__main__":
    main()
