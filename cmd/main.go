package main

import (
	"cloudfox-api/internal/http/routes"

	"github.com/gin-gonic/gin"
	"github.com/joho/godotenv"
)

func main() {
	godotenv.Load()
	router := gin.Default()
	routes.RegisterRoutes(router)
	router.Run(":8080")
}
