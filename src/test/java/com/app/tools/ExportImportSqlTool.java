package com.app.tools;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Exporta datos de MySQL a {@code src/main/resources/import.sql} (solo INSERT, sin DDL).
 * Uso: {@code ./mvnw -q test-compile exec:java -Dexec.mainClass=com.app.tools.ExportImportSqlTool -Dexec.classpathScope=test}
 * <p>
 * Credenciales: variables de entorno {@code SPRING_DATASOURCE_URL}, {@code SPRING_DATASOURCE_USERNAME},
 * {@code SPRING_DATASOURCE_PASSWORD} o valores por defecto alineados con {@code application.properties}.
 */
public final class ExportImportSqlTool {

	private static final String[] TABLES_IN_ORDER = { "club", "bancos", "usuarios", "categorias",
			"categoria_valor_vigencia", "usuarios_rol", "cuentas_bancarias", "deportistas", "temporadas",
			"orden_pago", "pago", "club_historial_cambio", "notification_config", "notification_send_log",
			"email_envios", "asistencia_clase", "no_pago_config", "pago_comprobante" };

	private ExportImportSqlTool() {
	}

	public static void main(String[] args) throws Exception {
		Class.forName("com.mysql.cj.jdbc.Driver");
		String url = envOr("SPRING_DATASOURCE_URL",
				"jdbc:mysql://127.0.0.1/bd_adm_club?serverTimezone=America/Santiago&useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=5000&socketTimeout=15000");
		String user = envOr("SPRING_DATASOURCE_USERNAME", "root");
		String pass = envOr("SPRING_DATASOURCE_PASSWORD", "Mysql.mysql");

		Path out = Path.of("src/main/resources/import.sql");
		if (!Files.isRegularFile(Path.of("pom.xml"))) {
			System.err.println("Ejecutar desde la raíz del proyecto Maven (donde está pom.xml).");
			System.exit(1);
		}

		StringBuilder sb = new StringBuilder();
		sb.append("-- Seed generado automáticamente (ExportImportSqlTool)\n");
		sb.append("-- Origen: ").append(maskPassword(url)).append(" usuario=").append(user).append("\n\n");
		sb.append("SET NAMES utf8mb4;\n");
		sb.append("SET FOREIGN_KEY_CHECKS=0;\n\n");

		try (Connection conn = DriverManager.getConnection(url, user, pass)) {
			for (String table : TABLES_IN_ORDER) {
				if (!tableExists(conn, table)) {
					continue;
				}
				long n = countRows(conn, table);
				if (n == 0) {
					continue;
				}
				sb.append("-- ").append(table).append(" (").append(n).append(")\n");
				dumpTable(conn, table, sb);
				sb.append("\n");
			}
		}

		sb.append("SET FOREIGN_KEY_CHECKS=1;\n");

		Files.createDirectories(out.getParent());
		Files.writeString(out, sb.toString(), StandardCharsets.UTF_8);
		System.out.println("Escrito: " + out.toAbsolutePath());
		// Evita hilos huérfanos del driver MySQL al ejecutar con exec:java
		System.exit(0);
	}

	private static String envOr(String key, String def) {
		String v = System.getenv(key);
		return (v != null && !v.isBlank()) ? v : def;
	}

	private static String maskPassword(String url) {
		return url.replaceAll("password=([^&]+)", "password=***");
	}

	private static boolean tableExists(Connection conn, String table) throws SQLException {
		String catalog = conn.getCatalog();
		var meta = conn.getMetaData();
		try (ResultSet trs = meta.getTables(catalog, null, table, new String[] { "TABLE" })) {
			return trs.next();
		}
	}

	private static long countRows(Connection conn, String table) throws SQLException {
		try (Statement st = conn.createStatement();
				ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM `" + table + "`")) {
			rs.next();
			return rs.getLong(1);
		}
	}

	private static void dumpTable(Connection conn, String table, StringBuilder sb) throws SQLException {
		try (Statement st = conn.createStatement();
				ResultSet rs = st.executeQuery("SELECT * FROM `" + table + "`")) {
			ResultSetMetaData md = rs.getMetaData();
			int colCount = md.getColumnCount();
			List<String> cols = new ArrayList<>();
			List<Integer> types = new ArrayList<>();
			for (int i = 1; i <= colCount; i++) {
				cols.add(md.getColumnLabel(i));
				types.add(md.getColumnType(i));
			}
			while (rs.next()) {
				sb.append("INSERT INTO `").append(table).append("` (`");
				sb.append(String.join("`, `", cols));
				sb.append("`) VALUES (");
				for (int i = 0; i < colCount; i++) {
					if (i > 0) {
						sb.append(", ");
					}
					sb.append(sqlLiteral(rs, i + 1, types.get(i)));
				}
				sb.append(");\n");
			}
		}
	}

	private static String sqlLiteral(ResultSet rs, int colIndex, int sqlType) throws SQLException {
		if (isBinaryType(sqlType)) {
			return "NULL";
		}
		Object val = rs.getObject(colIndex);
		if (rs.wasNull() || val == null) {
			return "NULL";
		}
		if (sqlType == Types.BIT || sqlType == Types.BOOLEAN) {
			if (val instanceof Boolean b) {
				return b ? "b'1'" : "b'0'";
			}
			if (val instanceof byte[] bytes && bytes.length > 0) {
				return (bytes[0] != 0) ? "b'1'" : "b'0'";
			}
			return "b'0'";
		}
		if (val instanceof Number) {
			return val.toString();
		}
		if (val instanceof LocalDateTime ldt) {
			return "'" + ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "'";
		}
		if (val instanceof LocalDate ld) {
			return "'" + ld + "'";
		}
		if (val instanceof Timestamp ts) {
			return "'" + ts.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "'";
		}
		if (val instanceof java.sql.Date d) {
			return "'" + d.toString() + "'";
		}
		if (val instanceof java.util.Date d) {
			return "'" + new Timestamp(d.getTime()).toString() + "'";
		}
		String s = val.toString();
		return "'" + escapeSql(s) + "'";
	}

	private static boolean isBinaryType(int sqlType) {
		return sqlType == Types.BINARY || sqlType == Types.VARBINARY || sqlType == Types.LONGVARBINARY
				|| sqlType == Types.BLOB;
	}

	private static String escapeSql(String s) {
		return s.replace("\\", "\\\\").replace("'", "''");
	}
}
