package routes

import (
	"cloudfox-api/internal/http/controllers"

	"github.com/gin-gonic/gin"
)

func RegisterRoutes(router *gin.Engine) {
	api := router.Group("/cloudfox-api/v1")
	api.GET("/login", controllers.LoginController{}.LoginUser)
}
