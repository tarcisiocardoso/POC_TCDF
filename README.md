# POC_TCDF
Prova de conceito para  TCDF

# Ambiente desenvolvimento
### Clonar o projeto do repositorio:
git clone
### Importar o projeto para IDE
Eclipse: file->import->Maven->Existing Maven Projects

### Arquivo de configuração 
criar um arquivo de proprieade com as configurações necessarias para execução, exemplo:

driver=org.postgresql.Driver
user=postgres
pass=123456
url=jdbc:postgresql://localhost:5432/postgres
workdir=./fonte

### Run desenv
Run configuration -> adiciona o caminho para o arquivo no program arguments

# Ambiente Produção
### Pegar a ultima versão do respositorio:
git pull
### Arquivo de configuração
Criar o arquivo de configuração com as configurações de acordo com a produção

### Instalar as novas atualizações:
mvn install
### Executar o jar gerado pelo passo acima:
jar -jar "arquivo_gerado_pelo_mvn_isntall" "file.properties"

