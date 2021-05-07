# Lempel-Ziv Compressor

## Leírás

Az alkalmazás a LZ77 Lempel-Ziv kódolás szemléltetésére készült és ebből kifolyólag nem tartalmaz semmilyen extra
optimalizálást. JVM-re épülő konzol alkalmazás, ami egy megadott file elkódolására vagy dekódolásra használható.
Egyszerre több file vagy mappában található file-okra való futtatása nem támogatott.

## Külső függőségek

Lévén, hogy az alkalmazás JVM alapú egy JRE vagy JDK megléte szükséges a futtatáshoz.

A Java verzióként az ```openjdk 11.0.9.1``` lett használva fejlesztéshez és teszteléshez.\
Bármelyik Java 11 standardnak megfelelő JRE vagy JDK megfelel a futtatáshoz.\
A Java verziót konzolon ```java --version``` paranccsal tudjuk ellenőrizni:
``` 
java --version
openjdk 11.0.9.1 2020-11-04 LTS
OpenJDK Runtime Environment 18.9 (build 11.0.9.1+1-LTS)
OpenJDK 64-Bit Server VM 18.9 (build 11.0.9.1+1-LTS, mixed mode)
```
Amennyiben több JDK/JRE is megtalálható a környezeti változók között mint java path, fontos hogy a 11-es JDK/JRE 
legyen legelsőként a sorban, mivel ez befolyásolja azt, hogy melyik verziót használ a konzol.

## Futtatás

Az alkalmazás egyetlen .jar file-ba kerül buildelés után, ezt futtatva az alkalmazás minden feature-e használható.
### Megjegyzés
Az alkalmazásnak van pár korláta:
* A bemeneti fájl maximum 2GB-os lehet
* A bemeneti fájl a UNICODE első 1023 karakterére működik az elvárásnak megfelelően

###Példa:
```
java -jar .\LempeZiv-1.0.0.jar -fc .\input_file_hu_large.txt
```

## Kapcsolók

Az alkalmazás a több kapcsolót is kapott, a bizonyos funciók közötti váltogatások érdekében.\
Ezeket a -h kapcsolón keresztűl érjük el a konzolon
```
java -jar .\LempeZiv-1.0.0.jar -h
```

###Jelenleg elérhető kapcsolók:

```
java -jar .\LempeZiv-1.0.0.jar -h
usage: [-h] [-t] [-f FILE] -c [-w WINDOW]

required arguments:
  -c, -d            Mode of operation: -c for compression, -d for
                    decompression


optional arguments:
  -h, --help        show this help message and exit

  -t, -v, -n        Modifies logging verbosity: -n turns off logging, -v for
                    VERBOSE (provides additional logging), -t for TRACE (most
                    detailed level, not recommended for big files). When no
                    flag is provided, the default INFO level is active.

  -f FILE,          Path of the input file. When no file is provided. The
  --file FILE       default test files are used to generated the output file.

  -w WINDOW,        Sets the size of the sliding window. Valid values are
  --window WINDOW   [5-2000]
```
###Megjegyzés:
A -t kapcsoló a legnagyobb felbontásban és részletességben logol a konzol-ra ez már egy közepes fájl méretnél is nehezen követhető.
Ajánlott csak kisméretű fájloknál használni.\
Hasonló problémákat okozhat ha -w nagyon kis értéket kap, mivel ekkor több hármassal lesz elkódolva az input file, ami 
szintén több logolást eredményez.

## Futtatási példák
Azon kapcsolók összevonthatók, amelyeket nem várnak egyéb bemeneti paramétert (sorrend nem mindegy):
```
java -jar .\LempeZiv-1.0.0.jar -cf .\input_file_hu_medium.txt -> OK
java -jar .\LempeZiv-1.0.0.jar -cfw 10 .\input_file_hu_medium.txt -> HIBA
java -jar .\LempeZiv-1.0.0.jar -fc .\input_file_hu_medium.txt -> HIBA
java -jar .\LempeZiv-1.0.0.jar -w 100 -f .\output_file_hu.lz -cd -> OK
```

#### Alap kódolás/dekódolás
```
java -jar .\LempeZiv-1.0.0.jar -cf .\input_file_hu_medium.txt
java -jar .\LempeZiv-1.0.0.jar -df .\output_file_hu.lz
```
#### Futtatás alap tesztfájllal
```
java -jar .\LempeZiv-1.0.0.jar -cv
```
#### Logolási szintek
```
java -jar .\LempeZiv-1.0.0.jar -dnf .\output_file_hu.lz
java -jar .\LempeZiv-1.0.0.jar -cf .\input_file_hu_medium.txt
java -jar .\LempeZiv-1.0.0.jar -dvf .\output_file_hu.lz
java -jar .\LempeZiv-1.0.0.jar -ctf .\input_file_hu_medium.txt
```

#### Csúszóablak méret
Csak kódoláshoz van használva.
```
java -jar .\LempeZiv-1.0.0.jar -cf .\input_file_hu_medium.txt -w 10
java -jar .\LempeZiv-1.0.0.jar -cf .\input_file_hu_medium.txt -w 1900
```

#### Kis fájl mérettel, kis ablakkal, részletes logolással
```
 java -jar .\LempeZiv-1.0.0.jar -ctf .\small_example.txt -w 5
```

#### Fájl kódolás, majd az elkódolt fájl dekódolása

```
 java -jar .\LempeZiv-1.0.0.jar -cf .\input_file_en_large.txt
 java -jar .\LempeZiv-1.0.0.jar -df .\compression-test--2021-05-04-19-49-356360237305.lz
```
