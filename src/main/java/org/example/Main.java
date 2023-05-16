package org.example;
import java.sql.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {

    public static void main(String[] args) {
        try {
            // Set the database path
            String dbPath = System.getProperty("user.dir") + "/covidInfo.db";

            // Create a connection to the database
            Connection connection = null;
            try {
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                // Create the table if it doesn't exist
                createTable(connection);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            scrapingData(connection);

            assert connection != null;

            Extract.ExtractDataToCSV("Asia", connection);
            //Extract.ExtractDataToCSV(connection);

            connection.close();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }
    public static void scrapingData(Connection connection) throws IOException {

        String url = "https://www.worldometers.info/coronavirus/";
        Document doc = Jsoup.connect(url).timeout(6000).get();
        Elements table = doc.select("table[id = main_table_countries_today]");

        for (Element row : table.select("tr:gt(7)")) {

            Elements tds = row.select("td:not([rowspan])");

            String region = row.select("td[style*=display:none]").text();
            String country = tds.get(1).text();

            String totalCases = tds.get(2).text().replace(",", "");

            String totalTests = tds.get(12).text().replace(",", "");

            String activeCases = tds.get(8).text().replace(",", "");

            String insertDataQuery = "INSERT INTO covidInfo (region, country, totalCases, " +
                    "totalTests, activeCases) VALUES (?, ?, ?, ?, ?)";

            try {
                assert connection != null;

                PreparedStatement statement = connection.prepareStatement(insertDataQuery);
                statement.setString(1, region);
                statement.setString(2, country);
                statement.setString(3, totalCases);
                statement.setString(4, totalTests);
                statement.setString(5, activeCases);
                statement.executeUpdate();

            } catch (Exception e) {
                continue;
            }
        }
    }

    static void createTable(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        String createTableQuery = "CREATE TABLE IF NOT EXISTS covidInfo (region TEXT, country TEXT UNIQUE, " +
                "totalCases TEXT, totalTests TEXT, activeCases TEXT)";
        statement.executeUpdate(createTableQuery);
        System.out.println("Database created successfully.");
    }
}