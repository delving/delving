#!/usr/bin/env sh

# This scrip deletes spurious empty folders that are left over when checking out the europeanaLabs version of the codebase.

echo 'go to root of project'
cd ../../

echo 'removing unwanted folders'
rm -rf integration-tests monitoring-tests portal-full portal-lite tools/multi-lingo uim

echo 'done'
