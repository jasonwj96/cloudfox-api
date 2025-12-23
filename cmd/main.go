package main

import (
	"context"
	"errors"
	"log"
	"log/slog"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"cloudfox-api/internal/http/controllers"
	"cloudfox-api/internal/http/routes"
	"cloudfox-api/internal/repository"
	"cloudfox-api/internal/repository/connectors"

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

	// Root context: ONLY for lifecycle & shutdown signaling
	rootCtx, stop := signal.NotifyContext(
		context.Background(),
		os.Interrupt,
		syscall.SIGTERM,
	)
	defer stop()

	// Infrastructure context: must not be signal-cancelled
	infraCtx := context.Background()

	router := gin.New()
	router.Use(gin.Logger(), gin.Recovery())

	pgxConnector, err := connectors.NewPGXConnector(infraCtx)
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
		<-rootCtx.Done()
		shutdownCtx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
		defer cancel()

		if err := srv.Shutdown(shutdownCtx); err != nil {
			slog.Error("server shutdown failed", "err", err)
		}
	}()

	if err := srv.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
		return err
	}

	return nil
}
