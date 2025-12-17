package main

import (
	"context"
	"log"

	"cloudfox-api/internal/http/controllers"
	"cloudfox-api/internal/http/routes"
	"cloudfox-api/internal/repository"
	"cloudfox-api/internal/repository/connectors"

	"github.com/gin-gonic/gin"
	"github.com/joho/godotenv"
)

func main() {
	_ = godotenv.Load()

	ctx := context.Background()

	// 1. Create DB connector ONCE
	connector, err := connectors.NewPostgresSQLConnector(ctx)
	if err != nil {
		log.Fatal(err)
	}
	defer connector.Close()

	// 2. Create repository
	accountRepo := repository.NewAccountRepository(
		connector.Pool(),
	)

	// 3. Create controller
	loginController := controllers.NewLoginController(
		accountRepo,
	)

	// 4. Router + routes
	router := gin.Default()
	routes.RegisterRoutes(router, loginController)

	_ = router.Run(":8080")
}
