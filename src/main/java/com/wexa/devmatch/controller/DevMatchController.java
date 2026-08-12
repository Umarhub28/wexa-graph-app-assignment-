package com.wexa.devmatch.controller;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class DevMatchController {

    private final Driver driver;

    public DevMatchController(Driver driver) {
        this.driver = driver;
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getUsers() {
        try (Session session = driver.session()) {
            String query = 
                "MATCH (u:User) " +
                "OPTIONAL MATCH (u)-[:KNOWS]->(k:Skill) " +
                "OPTIONAL MATCH (u)-[:WANTS_TO_LEARN]->(w:Skill) " +
                "RETURN u.name AS name, " +
                "       collect(DISTINCT k.name) AS knows, " +
                "       collect(DISTINCT w.name) AS wantsToLearn " +
                "ORDER BY u.name";
                
            List<Map<String, Object>> users = session.run(query).list(record -> Map.of(
                "name", record.get("name").asString(),
                "knows", record.get("knows").asList(),
                "wantsToLearn", record.get("wantsToLearn").asList()
            ));
            
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/matches/{username}")
    public ResponseEntity<List<Map<String, Object>>> getMatches(@PathVariable String username) {
        try (Session session = driver.session()) {
            // This query finds users who know what 'I' want to learn, 
            // AND want to learn what 'I' know. (4-hop traversal)
            // It uses proper parameterization ($username) to prevent injection.
            String query = 
                "MATCH (me:User {name: $username})-[:WANTS_TO_LEARN]->(targetSkill:Skill)" +
                "<-[:KNOWS]-(mentor:User)-[:WANTS_TO_LEARN]->(mySkill:Skill)<-[:KNOWS]-(me) " +
                "RETURN mentor.name AS mentorName, " +
                "       targetSkill.name AS iLearn, " +
                "       mySkill.name AS theyLearn";
                
            List<Map<String, Object>> matches = session.run(query, Map.of("username", username))
                .list(record -> Map.of(
                    "mentorName", record.get("mentorName").asString(),
                    "iLearn", record.get("iLearn").asString(),
                    "theyLearn", record.get("theyLearn").asString()
                ));
            
            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}