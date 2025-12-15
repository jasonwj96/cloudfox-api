package repository

import (
	"context"
	"errors"
	"fmt"
	"os"
	"strconv"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"
)

type PGXConnector struct {
	pool *pgxpool.Pool
}

func NewPostgresSQLConnector(ctx context.Context) (*PGXConnector, error) {
	host := os.Getenv("POSTGRESQL_HOST")
	port := os.Getenv("POSTGRESQL_PORT")
	user := os.Getenv("POSTGRESQL_USER")
	dbPass := os.Getenv("POSTGRESQL_PASS")
	dbname := os.Getenv("POSTGRESQL_DB_NAME")
	minConns := int32(1)
	maxConns := int32(10)
	sslmode := os.Getenv("POSTGRESQL_SSLMODE")

	if host == "" || port == "" || user == "" || dbPass == "" || dbname == "" {
		return nil, errors.New("missing required database environment variables")
	}

	if sslmode == "" {
		sslmode = "require"
	}

	connectionString := fmt.Sprintf(
		"host=%s port=%s user=%s password=%s dbname=%s sslmode=%s",
		host, port, user, dbPass, dbname, sslmode,
	)

	config, err := pgxpool.ParseConfig(connectionString)

	if err != nil {
		return nil, fmt.Errorf("parse pgxpool config: %w", err)
	}

	if s := os.Getenv("POSTGRESQL_MIN_CONNS"); s != "" {
		v, err := strconv.ParseInt(s, 10, 32)
		if err != nil {
			return nil, errors.New("POSTGRESQL_MIN_CONNS must be a number")
		}
		minConns = int32(v)
	}

	if s := os.Getenv("POSTGRESQL_MAX_CONNS"); s != "" {
		v, err := strconv.ParseInt(s, 10, 32)
		if err != nil {
			return nil, errors.New("POSTGRESQL_MAX_CONNS must be a number")
		}
		maxConns = int32(v)
	}

	if minConns > maxConns {
		return nil, errors.New("MIN_CONNS cannot be greater than MAX_CONNS")
	}

	config.MinConns = minConns
	config.MaxConns = maxConns
	config.MaxConnLifetime = time.Hour

	pool, err := pgxpool.NewWithConfig(ctx, config)

	if err != nil {
		return nil, err
	}

	if err := pool.Ping(ctx); err != nil {
		pool.Close()
		return nil, err
	}

	return &PGXConnector{pool: pool}, nil
}

func (c *PGXConnector) Close() {
	if c != nil && c.pool != nil {
		c.pool.Close()
	}
}
