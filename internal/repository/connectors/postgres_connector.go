package connectors

import (
	"context"
	"fmt"
	"os"
	"time"

	"github.com/bytedance/gopkg/util/logger"
	"github.com/jackc/pgx/v5/pgxpool"
)

type PGXConnector struct {
	Pool *pgxpool.Pool
}

func getVariable(key string) string {
	value, ok := os.LookupEnv(key)

	if !ok || value == "" {
		logger.Fatalf("%s environment variable is required", key)
	}

	return value
}

func NewPostgresSQLConnector(ctx context.Context) (*PGXConnector, error) {
	host := getVariable("POSTGRESQL_HOST")
	port := getVariable("POSTGRESQL_PORT")
	user := getVariable("POSTGRESQL_USER")
	dbPass := getVariable("POSTGRESQL_PASS")
	dbname := getVariable("POSTGRESQL_DB_NAME")
	sslmode := getVariable("POSTGRESQL_SSLMODE")

	connectionString := fmt.Sprintf(
		"host=%s port=%s user=%s password=%s dbname=%s sslmode=%s",
		host, port, user, dbPass, dbname, sslmode,
	)

	config, err := pgxpool.ParseConfig(connectionString)

	if err != nil {
		return nil, fmt.Errorf("parse pgxpool config: %w", err)
	}

	config.MaxConnLifetime = time.Hour

	pool, err := pgxpool.NewWithConfig(ctx, config)

	if err != nil {
		return nil, err
	}

	if err := pool.Ping(ctx); err != nil {
		pool.Close()
		return nil, err
	}

	return &PGXConnector{Pool: pool}, nil
}

func (c *PGXConnector) Close() {
	if c != nil && c.Pool != nil {
		c.Pool.Close()
	}
}
