mkdir -p build/classes
javac src/*.java -d build/classes
mkdir -p build/jar
cd build/classes
jar cmf ../../src/main-class.mf ../jar/PassVault.jar *
cd ../..
