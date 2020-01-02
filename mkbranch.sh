## Start with the master branch
git checkout master
git fetch origin
git reset --hard origin/master

## Create a new-branch
git checkout -b delta-fetch-feature
git status

## Update, add, commit, and push changes
git status
git add <some-file>
git commit

## Push feature branch to remote
git push -u origin delta-fetch-feature
 
