package main

import (
	"cloudfox-api/internal/http/controllers"
	"cloudfox-api/internal/http/routes"
	"cloudfox-api/internal/repository"
	"cloudfox-api/internal/repository/connectors"
	"context"
	"errors"
	"log"
	"log/slog"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/joho/godotenv"
)

func main() {
	if err := run(); err != nil {
		log.Fatal(err)
	}
}

func run() error {

	if err := godotenv.Load(); err != nil {
		slog.Info("no .env file found; using environment variables")
	}

	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)

	defer stop()

	router := gin.New()
	router.Use(gin.Logger(), gin.Recovery())

	pgxConnector, err := connectors.NewPGXConnector(ctx)

	if err != nil {
		return err
	}

	defer pgxConnector.Close()

	accountRepository := repository.NewAccountRepository(pgxConnector)
	loginController := controllers.NewLoginController(accountRepository)

	routes.RegisterRoutes(router, loginController)

	srv := &http.Server{
		Addr:         ":8080",
		Handler:      router,
		ReadTimeout:  5 * time.Second,
		WriteTimeout: 10 * time.Second,
		IdleTimeout:  60 * time.Second,
	}

	go func() {
		<-ctx.Done()
		shutdownCtx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
		defer cancel()
		_ = srv.Shutdown(shutdownCtx)
	}()

	err = srv.ListenAndServe()
	if err != nil && !errors.Is(err, http.ErrServerClosed) {
		return err
	}

	return nil
}
