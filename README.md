# How to start the system
## Prerequisites
- Docker
- Java 19 (LTS)
- Maven 3.8.2
- Python 3.9.13
- Pipenv 

## Start the system
To start the system, you need to start the following components in the following order:
1. The service registry and the message broker
Need to start the service registry and the message broker first, as they are required by the other services.

```cd infrastructure```

```docker compose up```

Login credentials for the message broker are:
- username: admin
- password: cas735

2. The parking services
Running the services with maven

```cd services/parking_receiver```

```mvn clean package```

```mvn spring-boot:run -Dspring-boot.run.profiles=local```
3. The parking simulator
Simulating different transponder readings

```cd clients/parking_simulator```

```pipenv shell```

```python ./parking_simulator.py localhost```
