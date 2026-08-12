package com.wexa.devmatch;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);
    private final Driver driver;

    public DatabaseSeeder(Driver driver) {
        this.driver = driver;
    }

    @Override
    public void run(String... args) {
        log.info("Checking database connection and seeding data...");
        
        try (Session session = driver.session()) {
            // 1. Clear existing data to ensure idempotency
            session.run("MATCH (n) DETACH DELETE n");

            // 2. Create Skills
            List<String> skills = List.of("React", "Node.js", "Python", "GraphDB", "Go", "AWS");
            session.run("UNWIND $skills AS skill MERGE (s:Skill {name: skill})", Map.of("skills", skills));

            // 3. Create Users and Relationships
            var users = List.of(
                Map.of("name", "Alice", "knows", List.of("React", "AWS"), "wants", List.of("GraphDB", "Go")),
                Map.of("name", "Bob", "knows", List.of("GraphDB", "Python"), "wants", List.of("React", "AWS")), // Mutual match with Alice
                Map.of("name", "Charlie", "knows", List.of("Go"), "wants", List.of("Node.js")),
                Map.of("name", "Diana", "knows", List.of("Node.js", "GraphDB"), "wants", List.of("Go")), // Mutual match with Charlie
                Map.of("name", "Eve", "knows", List.of("Python"), "wants", List.of("AWS"))
            );

            for (var u : users) {
                // Create user
                session.run("MERGE (u:User {name: $name})", Map.of("name", u.get("name")));
                
                // Create KNOWS relationships
                List<String> knows = (List<String>) u.get("knows");
                if (!knows.isEmpty()) {
                    session.run(
                        "MATCH (u:User {name: $name}) " +
                        "UNWIND $knows AS skillName " +
                        "MATCH (s:Skill {name: skillName}) " +
                        "MERGE (u)-[:KNOWS]->(s)",
                        Map.of("name", u.get("name"), "knows", knows)
                    );
                }

                // Create WANTS_TO_LEARN relationships
                List<String> wants = (List<String>) u.get("wants");
                if (!wants.isEmpty()) {
                    session.run(
                        "MATCH (u:User {name: $name}) " +
                        "UNWIND $wants AS skillName " +
                        "MATCH (s:Skill {name: skillName}) " +
                        "MERGE (u)-[:WANTS_TO_LEARN]->(s)",
                        Map.of("name", u.get("name"), "wants", wants)
                    );
                }
            }
            log.info("Database seeded successfully with Graph DB nodes and edges!");
        } catch (Exception e) {
            log.error("Failed to seed database. Please check your CognoDB credentials.", e);
        }
    }
}