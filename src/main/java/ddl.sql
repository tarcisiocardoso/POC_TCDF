-- Table: public.licitacao

CREATE TABLE arquivo(
	id serial NOT NULL PRIMARY KEY,
	nome VARCHAR(100),
	problema VARCHAR(200)
);

CREATE TABLE grupo(
	id serial NOT NULL PRIMARY KEY,
	idArquivo int,
	linha int,
	pagina int,
	nome VARCHAR(200),
	resumo VARCHAR(200),
	problema VARCHAR(200)
);

CREATE TABLE subGrupo(
	id serial NOT NULL PRIMARY KEY,
	idGrupo int,
	linha int,
	nome VARCHAR(200)
);


-- DROP TABLE public.licitacao;
CREATE TABLE registro
(
  id serial NOT NULL PRIMARY KEY,
  idSubGrupo int NOT NULL, 
  tipo character varying(100),
  dado json NOT NULL,
  conteudo text NOT NULL
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.licitacao
  OWNER TO tarcisio;

  
create or replace view teste_vw as (
select nomeArquivo, to_date(dataArquivo[1], 'DD-MM-YYYY') as dataArquivo, nomeGrupo, nomeSubGrupo, idRegistro, tipo, objeto, cast(valor[1] as FLOAT), conteudo from (
	select arquivo.nome nomeArquivo, regexp_matches(arquivo.nome, '\d\d-\d\d-\d\d\d\d') as dataArquivo, grupo.nome nomeGrupo, 
	    sb.nome nomeSubGrupo, reg.id as idRegistro, reg.tipo tipo, reg.dado ->> 'processo' as processo, 
	    regexp_matches(replace(replace(reg.dado ->> 'valor', '.', ''), ',', '.'), '\d+\.\d+') as valor, 
	    reg.dado ->> 'objeto' as objeto, reg.conteudo as conteudo 
from arquivo
	inner join grupo on grupo.idArquivo = arquivo.id
	inner join subGrupo sb on sb.idGrupo = grupo.id
	inner join registro reg on reg.idSubGrupo = sb.id
	where tipo like '%PREGÃO%' and ((dado ->> 'evento') = 'abertura') and ((dado ->> 'responsavel') is not null)) as tb
where processo is not null
and valor is not null);
