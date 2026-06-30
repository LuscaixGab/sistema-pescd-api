package br.ufscar.dc.dsw.pescd.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Profile("!test")
public class PlanoTrabalhoSchemaMigration implements CommandLineRunner {
//essa classe aqui vai mudar para a inscricao nao seja not null
    //fiz isso porque na epoca a Rafa ainda nn tinha terminado a insercao de alunos

    private final JdbcTemplate jdbcTemplate;

    public PlanoTrabalhoSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        List<Map<String, Object>> colunas = jdbcTemplate.queryForList("""
                SELECT IS_NULLABLE, COLUMN_TYPE
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'planos_trabalho'
                  AND COLUMN_NAME = 'inscricao_id'
                """);

        if (colunas.isEmpty()) {
            return;
        }

        String isNullable = String.valueOf(colunas.get(0).get("IS_NULLABLE"));
        if ("YES".equalsIgnoreCase(isNullable)) {
            return;
        }

        String columnType = String.valueOf(colunas.get(0).get("COLUMN_TYPE"));
        jdbcTemplate.execute("ALTER TABLE planos_trabalho MODIFY COLUMN inscricao_id " + columnType + " NULL");
    }
}
