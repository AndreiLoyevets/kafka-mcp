# Kafka MCP Server

A Model Context Protocol (MCP) server that provides AI assistants like Claude with read-only access to Apache Kafka cluster information.
This server enables AI assistants to monitor and inspect Kafka cluster status, nodes, and topics.

## Features

- **Cluster Controller Information**: Get details about the Kafka cluster controller node
- **Cluster Nodes**: Retrieve information about all nodes in the Kafka cluster
- **Topic Management**: List and describe Kafka topics with detailed metadata
- **Read-Only Operations**: Safely provides cluster information without allowing modifications
- **Spring Boot Integration**: Built with Spring Boot and Spring AI MCP framework

## Available MCP Tools

The server exposes the following tools to AI assistants:

1. **Describe Kafka cluster controller** - Get Kafka cluster controller node details
2. **Describe Kafka cluster nodes** - Get all Kafka cluster nodes details  
3. **List topics** - List all Kafka topics in the cluster
4. **Describe topics** - Get detailed information for specific Kafka topics

## Prerequisites

- Java 25 or higher
- Maven 3.6+
- Access to a Kafka cluster
- Claude Desktop (for Claude integration) or another MCP-compatible AI assistant

## Configuration

### Kafka Connection

Example of configuring your Kafka connection in `kafka-mcp-server/src/main/resources/application.yaml`
or env-specific `application-<env>.yaml`, with SSL and JKS keys:

```yaml
spring:
  kafka:
    bootstrap-servers: <bootstrap servers>
    security:
      protocol: SSL
    ssl:
      bundle: kafka-bundle
  ssl:
    bundle:
      jks:
        kafka-bundle:
          key:
            password: <key password>
          keystore:
            type: JKS
            location: <path to keystore>
            password: <keystore password>
          truststore:
            type: JKS
            location: <path to truststore>
            password: <truststore password>
```

### Environment-Specific Configuration

The project includes configuration files for different environments:
- `application-local.yaml` - Local development
- `application.yaml` - Default configuration

## Building and Running

### Build the Project

```bash
mvn clean package
```

### Run the Server

```bash
cd kafka-mcp-server
mvn spring-boot:run
```

The server will start on `http://localhost:8080` with the MCP endpoint available at `/mcp`.

## Claude Desktop Integration

To integrate with Claude Desktop, add the following configuration to your Claude Desktop config file:

```json
{
  "mcpServers": {
    "my-kafka-mcp-server": {
      "command": "npx",
      "args": [
        "-y",
        "mcp-remote",
        "http://localhost:8080/mcp"
      ]
    }
  }
}
```

The configuration file is also available at `kafka-mcp-server/src/main/resources/claude-desktop-config.json`.

## Usage Examples

Once connected to Claude Desktop, you can ask questions like:

- "Check the health of my Kafka cluster"
- "Show me the Kafka cluster controller information"
- "List all topics in my Kafka cluster"
- "Describe the topic named 'user-events'"
- "What are all the nodes in my Kafka cluster?"

## Dependencies

- **Spring Boot 3.5.9** - Application framework
- **Spring AI 1.1.2** - AI integration framework
- **Spring Kafka** - Kafka integration
- **Spring Boot Actuator** - Monitoring and management

## Security Considerations

- This server provides **read-only** access to Kafka cluster information
- No write operations are supported to ensure cluster safety
- Configure appropriate network access controls for the server
- Ensure Kafka client credentials are properly secured