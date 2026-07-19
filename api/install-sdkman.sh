#!/bin/bash

echo "[1/4] Verificando dependências..."

if ! command -v zip >/dev/null 2>&1; then
  sudo apt install -y zip
fi

if ! command -v unzip >/dev/null 2>&1; then
sudo apt install unzip
fi

echo "[2/4] Instalando SDKMAN..."

if [ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
  echo "SDKMAN já está instalado."
else
  curl -s "https://get.sdkman.io" | bash >/dev/null 2>&1
fi

echo "[3/4] Carregando SDKMAN..."
if [ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
  source "$HOME/.sdkman/bin/sdkman-init.sh"
fi

echo "Instalando Maven..."
sdk install maven

echo "Concluido 👏"
