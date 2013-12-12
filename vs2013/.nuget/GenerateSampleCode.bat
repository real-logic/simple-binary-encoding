@echo off

echo Generating C# classes for TestSchema.xml...

java -Dsbe.output.dir=..\sample\output\ -Dsbe.target.language=CSHARP -jar ..\sbetool\SBETool.jar ..\sample\TestSchema.xml

echo Code generated in \sample\output\

pause