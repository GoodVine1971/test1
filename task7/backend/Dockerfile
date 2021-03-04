# https://hub.docker.com/_/microsoft-dotnet
FROM mcr.microsoft.com/dotnet/sdk:3.1 AS build
WORKDIR /source

# copy csproj and restore as distinct layers
COPY *.sln .
COPY ExadelBonusPlus.DataAccess/*.csproj ./ExadelBonusPlus.DataAccess/
COPY ExadelBonusPlus.Services/*.csproj ./ExadelBonusPlus.Services/
COPY ExadelBonusPlus.Services.Models/*.csproj ./ExadelBonusPlus.Services.Models/
COPY ExadelBonusPlus.Services.Tests/*.csproj ./ExadelBonusPlus.Services.Tests/
COPY ExadelBonusPlus.WebApi/*.csproj ./ExadelBonusPlus.WebApi/

RUN dotnet restore

# copy everything else and build app
#COPY ExadelBonusPlus.DataAccess/. ./ExadelBonusPlus.DataAccess/
#COPY ExadelBonusPlus.Services/. ./ExadelBonusPlus.Services/
#COPY ExadelBonusPlus.Services.Models/. ./ExadelBonusPlus.Services.Models/
#COPY ExadelBonusPlus.Services.Tests/. ./ExadelBonusPlus.Services.Tests/
COPY ExadelBonusPlus.WebApi/. ./ExadelBonusPlus.WebApi/

#WORKDIR /source/ExadelBonusPlus.DataAccess
#RUN dotnet publish -c release -o /app --no-restore
#WORKDIR /source/ExadelBonusPlus.Services
#RUN dotnet publish -c release -o /app --no-restore
#WORKDIR /source/ExadelBonusPlus.Services.Models
#RUN dotnet publish -c release -o /app --no-restore

WORKDIR /source/ExadelBonusPlus.WebApi
RUN dotnet publish -c release -o /app --no-restore

# final stage/image
FROM mcr.microsoft.com/dotnet/aspnet:3.1
WORKDIR /app
COPY --from=build /app ./
ENTRYPOINT ["dotnet", "ExadelBonusPlus.WebApi.dll"]

