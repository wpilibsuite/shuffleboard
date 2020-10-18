jar tf $2 | grep '\.so\|\.dylib\|\.jnilib' >sign.txt

while read f; do
  jar xf $2 $f
  codesign --force --timestamp --options=runtime -s $1 -v $f
done <sign.txt

jar uf $2 $(paste -s -d ' ' sign.txt)

while read f; do
  rm -rf $f
done <sign.txt

rm sign.txt
