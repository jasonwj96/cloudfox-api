package routes

import (
	"cloudfox-api/internal/http/controllers"
	"context"

	"github.com/gin-gonic/gin"
)

func RegisterRoutes(router *gin.Engine, ctx context.Context) error {

	loginController, err := controllers.NewLoginController(ctx)

	if err != nil {
		return err
	}

	api := router.Group("/cloudfox-api/v1")

	api.POST("/login", loginController.LoginUser)

	return nil
}
