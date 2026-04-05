#!/bin/bash

# DAMS Docker Setup Helper Script
# Usage: chmod +x docker-scripts.sh && ./docker-scripts.sh <command>

set -e

COMPOSE_FILE="docker-compose.yml"
PROJECT_NAME="dams"

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Helper functions
print_header() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}\n"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

# Commands
start() {
    print_header "Starting DAMS Services"
    
    print_info "Removing old containers..."
    docker-compose down --remove-orphans 2>/dev/null || true
    
    print_info "Building and starting services..."
    docker-compose up --build
}

start_bg() {
    print_header "Starting DAMS Services (Background)"
    
    print_info "Removing old containers..."
    docker-compose down --remove-orphans 2>/dev/null || true
    
    print_info "Building and starting services in background..."
    docker-compose up -d --build
    
    print_success "Services started in background"
    echo -e "\n${YELLOW}Monitor logs with:${NC} docker-compose logs -f"
}

stop() {
    print_header "Stopping DAMS Services"
    
    docker-compose stop
    print_success "All services stopped"
}

down() {
    print_header "Removing DAMS Services and Volumes"
    
    docker-compose down -v
    print_success "All services and volumes removed"
}

rebuild() {
    print_header "Rebuilding All Services"
    
    docker-compose down -v 2>/dev/null || true
    docker system prune -f
    docker-compose up --build
}

rebuild_backend() {
    print_header "Rebuilding Backend Only"
    
    docker-compose build backend --no-cache
    docker-compose up -d backend
    print_success "Backend rebuilt"
}

rebuild_frontend() {
    print_header "Rebuilding Frontend Only"
    
    docker-compose build frontend --no-cache
    docker-compose up -d frontend
    print_success "Frontend rebuilt"
}

logs() {
    print_header "Tailing Logs from All Services"
    docker-compose logs -f
}

logs_backend() {
    print_header "Tailing Backend Logs"
    docker-compose logs -f backend
}

logs_frontend() {
    print_header "Tailing Frontend Logs"
    docker-compose logs -f frontend
}

logs_db() {
    print_header "Tailing Database Logs"
    docker-compose logs -f postgres
}

status() {
    print_header "Service Status"
    docker-compose ps
}

health() {
    print_header "Checking Service Health"
    
    echo -e "${YELLOW}Backend Health:${NC}"
    docker-compose exec backend curl -s http://localhost:8080/actuator/health | jq . || echo "Backend unreachable"
    
    echo -e "\n${YELLOW}Database Health:${NC}"
    docker-compose exec postgres pg_isready -U myuser || echo "Database unreachable"
    
    echo -e "\n${YELLOW}Frontend Status:${NC}"
    docker-compose exec frontend nginx -T && echo "Nginx: OK" || echo "Nginx: ERROR"
}

shell_backend() {
    print_header "Entering Backend Container"
    docker-compose exec backend bash
}

shell_frontend() {
    print_header "Entering Frontend Container"
    docker-compose exec frontend sh
}

shell_db() {
    print_header "Entering PostgreSQL Container"
    docker-compose exec postgres psql -U myuser -d mydb
}

clean() {
    print_header "Cleaning Up Docker Resources"
    
    print_info "Stopping running containers..."
    docker-compose down -v || true
    
    print_info "Removing dangling images..."
    docker image prune -f
    
    print_info "Removing dangling volumes..."
    docker volume prune -f
    
    print_success "Cleanup complete"
}

test_connectivity() {
    print_header "Testing Inter-Service Connectivity"
    
    echo -e "${YELLOW}Testing Frontend → Backend:${NC}"
    docker-compose exec frontend wget -q -O- http://backend:8080/actuator/health | head -c 100
    echo ""
    
    echo -e "\n${YELLOW}Testing Backend → Database:${NC}"
    docker-compose exec backend nc -zv postgres 5432
}

urls() {
    print_header "Service URLs"
    
    echo -e "${GREEN}Frontend (React App):${NC}"
    echo "  http://localhost:3000"
    
    echo -e "\n${GREEN}Backend API:${NC}"
    echo "  http://localhost:8080"
    echo "  http://localhost:8080/actuator/health"
    echo "  http://localhost:8080/api/..."
    
    echo -e "\n${GREEN}Database (PostgreSQL):${NC}"
    echo "  Host: localhost"
    echo "  Port: 5432"
    echo "  User: myuser"
    echo "  Password: mypassword"
    echo "  Database: mydb"
}

show_help() {
    echo "DAMS Docker Management Script"
    echo ""
    echo "Usage: ./docker-scripts.sh <command>"
    echo ""
    echo "Commands:"
    echo "  start            - Start all services (foreground)"
    echo "  start-bg         - Start all services (background)"
    echo "  stop             - Stop all services"
    echo "  down             - Stop and remove containers and volumes"
    echo "  rebuild          - Clean rebuild everything"
    echo "  rebuild-backend  - Rebuild backend only"
    echo "  rebuild-frontend - Rebuild frontend only"
    echo ""
    echo "Logs:"
    echo "  logs             - View logs from all services"
    echo "  logs-backend     - View backend logs"
    echo "  logs-frontend    - View frontend logs"
    echo "  logs-db          - View database logs"
    echo ""
    echo "Diagnostics:"
    echo "  status           - Show service status"
    echo "  health           - Check service health"
    echo "  test-conn        - Test inter-service connectivity"
    echo "  urls             - Show service URLs"
    echo ""
    echo "Access:"
    echo "  shell-backend    - Enter backend container (bash)"
    echo "  shell-frontend   - Enter frontend container (sh)"
    echo "  shell-db         - Enter database container (psql)"
    echo ""
    echo "Maintenance:"
    echo "  clean            - Remove dangling docker resources"
    echo "  help             - Show this help message"
}

# Main
case "${1:-help}" in
    start)
        start
        ;;
    start-bg)
        start_bg
        ;;
    stop)
        stop
        ;;
    down)
        down
        ;;
    rebuild)
        rebuild
        ;;
    rebuild-backend)
        rebuild_backend
        ;;
    rebuild-frontend)
        rebuild_frontend
        ;;
    logs)
        logs
        ;;
    logs-backend)
        logs_backend
        ;;
    logs-frontend)
        logs_frontend
        ;;
    logs-db)
        logs_db
        ;;
    status)
        status
        ;;
    health)
        health
        ;;
    test-conn)
        test_connectivity
        ;;
    urls)
        urls
        ;;
    shell-backend)
        shell_backend
        ;;
    shell-frontend)
        shell_frontend
        ;;
    shell-db)
        shell_db
        ;;
    clean)
        clean
        ;;
    help)
        show_help
        ;;
    *)
        print_error "Unknown command: $1"
        show_help
        exit 1
        ;;
esac
