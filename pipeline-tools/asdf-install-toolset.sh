#!/usr/bin/env bash
set -eo pipefail

usage() {
    echo "Usage: $(basename "$0") [-h] [-g] [-l] <DIR>" 1>&2
    exit 1
}

add_plugins() {
    local file
    local name
    local git_url
    file=$1

    if test -e "$file"; then
        echo "Setting up asdf plugins at $file"
        while IFS= read -r line; do
            echo "read"
            name=$(echo "$line" | awk '{print $1}')
            git_url=$(echo "$line" | awk '{print $2}')
            echo "Adding plugin '$name' ${git_url:+from url $git_url}"
            asdf plugin add "$name" "$git_url"
        done < <(cat "$file")
    else
        echo "No $file file found"
    fi
}

install_versions() {
    local file
    file=$1

    if test -e "$file"; then
        echo "Installing asdf tool versions at $file"
        cp $file ./.tool-versions
        asdf install
        asdf current
    else
        echo "No $file file found"
    fi
}

set_versions_as() {
    local scope
    local file
    scope=$1
    file=$2

    if test -e "$file"; then
        echo "Setting as $scope asdf tool versions at $file"
        while IFS= read -r line; do
            name=$(echo "$line" | awk '{print $1}')
            version=$(echo "$line" | awk '{print $2}')
            echo "Setting '$name:$version' as $scope"
            asdf "$scope" "$name" "$version"
        done < <(cat "$file")
    fi
}

# MAIN
# Parse user options
while getopts ":hgl" o; do
    case "${o}" in
        g)   ADFS_SET_VERSIONS_AS=global;;
        l)   ADFS_SET_VERSIONS_AS=local;;
        h|*) usage;;
    esac
done
shift $((OPTIND-1))

# Make sure there is only one argument left (DIR)
if [[ $# -ne 1 ]]; then
    usage
fi

WDIR="$(realpath "$1")"

# If DIR is not a valid directory
if ! test -d "$WDIR"; then
    echo "Error: <DIR> must be a valid directory: $1" 1>&2
    usage
fi

ADFS_TOOL_PLUGINS_FILE="$WDIR/.tool-plugins"
ADFS_TOOL_VERSIONS_FILE="$WDIR/.tool-versions"

# Install plugins and tool versions
add_plugins "$ADFS_TOOL_PLUGINS_FILE"
install_versions "$ADFS_TOOL_VERSIONS_FILE"

# Set as global/local if specified by the user
if [ -n "$ADFS_SET_VERSIONS_AS" ]; then
    flag=${ADFS_SET_VERSIONS_AS/--/}
    set_versions_as "$flag" "$ADFS_TOOL_VERSIONS_FILE"
fi
