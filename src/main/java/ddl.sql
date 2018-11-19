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

  
  