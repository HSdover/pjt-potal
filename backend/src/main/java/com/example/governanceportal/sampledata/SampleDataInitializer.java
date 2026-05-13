package com.example.governanceportal.sampledata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConditionalOnProperty(prefix = "app.sample-data", name = "enabled", havingValue = "true", matchIfMissing = true)
class SampleDataInitializer implements ApplicationRunner {

    private static final String SAMPLE_CSV_NAME = "hira_institution_open_status_sample_1000_h2.csv";
    private static final String CLASSPATH_SAMPLE_CSV = "sample-data/hira/" + SAMPLE_CSV_NAME;
    private static final String INSERT_SQL = """
        INSERT INTO hira_institution_sample (
            encrypted_provider_id,
            institution_name,
            institution_type,
            sido_name,
            sigungu_name,
            road_address,
            specialty_name,
            opened_date
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private final JdbcTemplate jdbcTemplate;
    private final String configuredCsvPath;

    SampleDataInitializer(
        JdbcTemplate jdbcTemplate,
        @Value("${app.sample-data.hira-csv-path:}") String configuredCsvPath
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.configuredCsvPath = configuredCsvPath;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Integer rowCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM hira_institution_sample",
            Integer.class
        );

        if (rowCount != null && rowCount > 0) {
            return;
        }

        try (BufferedReader reader = openSampleCsv()) {
            loadSampleCsv(reader);
        }
    }

    private BufferedReader openSampleCsv() throws IOException {
        for (Path candidate : csvPathCandidates()) {
            if (Files.isRegularFile(candidate)) {
                return Files.newBufferedReader(candidate, StandardCharsets.UTF_8);
            }
        }

        ClassPathResource resource = new ClassPathResource(CLASSPATH_SAMPLE_CSV);
        if (resource.exists()) {
            return new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
        }

        throw new IllegalStateException(
            "HIRA sample CSV not found. Set GOVERNANCE_HIRA_SAMPLE_CSV or include " + CLASSPATH_SAMPLE_CSV
        );
    }

    private List<Path> csvPathCandidates() {
        List<Path> candidates = new ArrayList<>();

        if (StringUtils.hasText(configuredCsvPath)) {
            candidates.add(Path.of(configuredCsvPath));
        }

        candidates.add(Path.of("data", "processed", "hira", SAMPLE_CSV_NAME));
        candidates.add(Path.of("..", "data", "processed", "hira", SAMPLE_CSV_NAME));

        return candidates;
    }

    private void loadSampleCsv(BufferedReader reader) throws IOException {
        String header = reader.readLine();
        if (header == null) {
            return;
        }

        List<Object[]> batch = new ArrayList<>(200);
        String line;
        while ((line = reader.readLine()) != null) {
            List<String> fields = parseCsvLine(line);
            if (fields.size() != 8) {
                throw new IllegalStateException("Invalid HIRA sample CSV row: expected 8 columns, got " + fields.size());
            }

            batch.add(new Object[] {
                fields.get(0),
                fields.get(1),
                fields.get(2),
                fields.get(3),
                fields.get(4),
                fields.get(5),
                fields.get(6),
                toSqlDate(fields.get(7)),
            });

            if (batch.size() == 200) {
                flush(batch);
            }
        }

        flush(batch);
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        values.add(current.toString());
        return values;
    }

    private Date toSqlDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return Date.valueOf(LocalDate.parse(value.trim()));
    }

    private void flush(List<Object[]> batch) {
        if (batch.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(INSERT_SQL, batch);
        batch.clear();
    }
}
