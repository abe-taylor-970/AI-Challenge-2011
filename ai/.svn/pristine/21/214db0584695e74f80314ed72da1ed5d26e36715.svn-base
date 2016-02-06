@echo off
rem clean
del *.class
del MyBot.jar
rem compile
javac -cp C:\installs\log4j\log4j.jar;. MyBot.java
rem package
jar cvfm MyBot.jar Manifest.txt *.class 
rem clean
del *.class
