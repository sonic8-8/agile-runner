#!/usr/bin/env bash

set -euo pipefail

SOURCE_HOST="${SOURCE_HOST:-lab.ssafy.com}"
SOURCE_REPO="${SOURCE_REPO:-s14-fintech-finance-sub1/S14P21C103}"

tmp_source="$(mktemp)"
tmp_target="$(mktemp)"
trap 'rm -f "$tmp_source" "$tmp_target"' EXIT

GITLAB_HOST="$SOURCE_HOST" glab label list -R "$SOURCE_REPO" -F json > "$tmp_source"
gh api repos/sonic8-8/agile-runner/labels?per_page=100 > "$tmp_target"

python3 - <<'PY' "$tmp_source" "$tmp_target"
import json
import subprocess
import sys
from urllib.parse import quote

source_path, target_path = sys.argv[1], sys.argv[2]

with open(source_path, encoding="utf-8") as f:
    source_labels = json.load(f)

with open(target_path, encoding="utf-8") as f:
    target_labels = json.load(f)

source_by_name = {}
for label in source_labels:
    source_by_name[label["name"]] = {
        "color": label["color"].lstrip("#"),
        "description": label.get("description", "") or ""
    }

target_names = {label["name"] for label in target_labels}

for name, meta in source_by_name.items():
    subprocess.run(
        [
            "gh", "label", "create", name,
            "--color", meta["color"],
            "--description", meta["description"],
            "--force"
        ],
        check=True
    )

for name in sorted(target_names - source_by_name.keys()):
    subprocess.run(
        ["gh", "label", "delete", name, "--yes"],
        check=True
    )
PY
