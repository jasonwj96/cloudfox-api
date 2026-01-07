package connectors

import (
	"context"
	"fmt"
	"os"
	"strconv"
	"time"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

type PGXConnector struct {
	pool *pgxpool.Pool
}

func requireEnv(key string) (string, error) {
	v := os.Getenv(key)

	if v == "" {
		return "", fmt.Errorf("environment variable %s is required", key)
	}

	return v, nil
}

func requireEnvInt(key string) (int32, error) {
	v := os.Getenv(key)
	if v == "" {
		return 0, fmt.Errorf("environment variable %s is required", key)
	}

	i, err := strconv.Atoi(v)
	if err != nil {
		return 0, fmt.Errorf("environment variable %s must be an integer", key)
	}

	if i < 0 {
		return 0, fmt.Errorf("environment variable %s must be >= 0", key)
	}

	return int32(i), nil
}

func NewPGXConnector(ctx context.Context) (*PGXConnector, error) {
	host, err := requireEnv("POSTGRESQL_HOST")

	if err != nil {
		return nil, err
	}

	port, err := requireEnv("POSTGRESQL_PORT")

	if err != nil {
		return nil, err
	}

	user, err := requireEnv("POSTGRESQL_USER")

	if err != nil {
		return nil, err
	}

	pass, err := requireEnv("POSTGRESQL_PASS")

	if err != nil {
		return nil, err
	}

	dbname, err := requireEnv("POSTGRESQL_DB_NAME")

	if err != nil {
		return nil, err
	}

	sslmode, err := requireEnv("POSTGRESQL_SSLMODE")

	if err != nil {
		return nil, err
	}

	minConns, err := requireEnvInt("POSTGRESQL_MIN_CONNS")

	if err != nil {
		return nil, err
	}

	maxConns, err := requireEnvInt("POSTGRESQL_MAX_CONNS")

	if err != nil {
		return nil, err
	}

	if minConns > maxConns {
		return nil, fmt.Errorf("POSTGRESQL_MIN_CONNS cannot be greater than POSTGRESQL_MAX_CONNS")
	}

	connString := fmt.Sprintf(
		"host=%s port=%s user=%s password=%s dbname=%s sslmode=%s",
		host, port, user, pass, dbname, sslmode,
	)

	config, err := pgxpool.ParseConfig(connString)

	if err != nil {
		return nil, fmt.Errorf("parse pgxpool config: %w", err)
	}

	config.MinConns = minConns
	config.MaxConns = maxConns
	config.MaxConnLifetime = time.Hour
	config.MaxConnIdleTime = 30 * time.Minute
	config.HealthCheckPeriod = 1 * time.Minute

	pool, err := pgxpool.NewWithConfig(ctx, config)

	if err != nil {
		return nil, fmt.Errorf("create pgx pool: %w", err)
	}

	err = pool.Ping(ctx)

	if err != nil {
		pool.Close()
		return nil, fmt.Errorf("ping postgres: %w", err)
	}

	return &PGXConnector{pool: pool}, nil
}

func (c *PGXConnector) QueryRow(ctx context.Context, sql string, args ...any) pgx.Row {
	return c.pool.QueryRow(ctx, sql, args...)
}

func (c *PGXConnector) Exec(
	ctx context.Context,
	sql string,
	args ...any,
) error {
	_, err := c.pool.Exec(ctx, sql, args...)
	return err
}

func (c *PGXConnector) Close() {
	if c != nil && c.pool != nil {
		c.pool.Close()
	}
}
