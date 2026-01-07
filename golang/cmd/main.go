package main

import (
	"context"
	"errors"
	"log/slog"
	"net"
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
		slog.Error("fatal error", "err", err)
		os.Exit(1)
	}
}

func run() error {
	if err := godotenv.Load(); err != nil {
		slog.Info("no .env file found; using environment variables")
	}

	ctx, stop := signal.NotifyContext(
		context.Background(),
		os.Interrupt,

		syscall.SIGTERM,
	)
	defer stop()

	router := gin.New()
	router.Use(gin.Recovery())

	pgxConnector, err := connectors.NewPGXConnector(ctx)
	if err != nil {
		return err
	}

	accountRepository := repository.NewAccountRepository(pgxConnector)
	loginController := controllers.NewLoginController(accountRepository)
	routes.RegisterRoutes(router, loginController)

	srv := &http.Server{
		Addr:         ":8080",
		Handler:      router,
		ReadTimeout:  5 * time.Second,
		WriteTimeout: 10 * time.Second,
		IdleTimeout:  60 * time.Second,
		BaseContext: func(net.Listener) context.Context {
			return ctx
		},
	}

	go func() {
		<-ctx.Done()

		shutdownCtx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
		defer cancel()

		if err := srv.Shutdown(shutdownCtx); err != nil {
			slog.Error("server shutdown failed", "err", err)
		}

		pgxConnector.Close()
	}()

	slog.Info("server starting", "addr", srv.Addr)

	if err := srv.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
		return err
	}

	return nil
}
