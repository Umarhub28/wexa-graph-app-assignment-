DevMatch: Mutual Skill-Swap Mentorship (Java & Spring Boot)
DevMatch is a full-stack application built for the Wexa AI Take-Home Assignment. It leverages CognoDB and Java/Spring Boot to connect developers based on mutual learning goals, matching them for reciprocal mentorship.
 Hosted Demo & Repository
GitHub Repository: [Insert your repo URL here]
Live Demo: [Insert your Render.com hosted demo URL here]
Why a Graph Database?
In a traditional Relational Database (RDBMS), modeling users and skills requires junction tables (e.g., UserSkills, UserDesiredSkills). To answer the core question: "Find me a user who knows the skill I want to learn, AND who wants to learn the skill I already know" requires a complex, multi-hop self-join across multiple tables.
In a Graph Database, this problem maps perfectly to the way we naturally think about connections. The multi-hop query executes efficiently through index-free adjacency, demonstrating exactly where graph databases earn their place over relational schemas.
Data Model Diagram
          [:KNOWS]
(User) -----------------> (Skill)
  |                         ^
  |                         |
  +---[:WANTS_TO_LEARN]-----+


Core Cypher Queries Explained
Finding Mutual Matches (Multi-hop traversal)
This query finds the perfect skill swap using parameterized parameters via the official Neo4j Java driver to prevent injection (DevMatchController.java):
MATCH (me:User {name: $username})-[:WANTS_TO_LEARN]->(targetSkill:Skill)<-[:KNOWS]-(mentor:User)-[:WANTS_TO_LEARN]->(mySkill:Skill)<-[:KNOWS]-(me)
RETURN mentor.name AS mentorName, targetSkill.name AS iLearn, mySkill.name AS theyLearn


Setup & Run Instructions
1. Database Setup
Create a free account at CognoDB Cloud.
Provision a free (c0) instance.
Save the connection URI and the generated password for the cognodb user.
2. Application Configuration
Set your environment variables locally or in your deployment server:
NEO4J_URI = bolt+s://<your-instance-id>.databases.cognodb.cloud
NEO4J_USER = cognodb
NEO4J_PASSWORD = <your-password>
3. Running Locally
Clone the repository.
Build and run using Maven:
mvn clean spring-boot:run


Open your browser and navigate to: http://localhost:8080
