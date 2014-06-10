xcopy ..\..\..\packages\Google.ProtocolBuffers.2.4.1.521\tools\protoc.exe . /d /y
xcopy ..\..\..\packages\Google.ProtocolBuffers.2.4.1.521\tools\ProtoGen.exe . /d /y
xcopy ..\..\..\packages\Google.ProtocolBuffers.2.4.1.521\tools\*.dll . /d /y

protogen .\protos\sample-fix.proto .\protos\google\protobuf\csharp_options.proto .\protos\google\protobuf\descriptor.proto --proto_path=.\protos

pause