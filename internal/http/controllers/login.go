package controllers

import (
	"database/sql"
	"fmt"
	"os"
	"time"

	_ "github.com/lib/pq"

	"github.com/gin-gonic/gin"
)

func LoginUser(context *gin.Context) {
	host := os.Getenv("DB_HOST")
	port := os.Getenv("DB_PORT")
	user := os.Getenv("DB_USER")
	dbPass := os.Getenv("DB_PASS")
	dbname := os.Getenv("DB_NAME")
	driver := os.Getenv("DB_DRIVER")

	dsn := fmt.Sprintf(
		"host=%s port=%s user=%s password=%s dbname=%s sslmode=disable",
		host, port, user, dbPass, dbname,
	)

	db, err := sql.Open(driver, dsn)
	if err != nil {
		context.JSON(500, gin.H{"error": err.Error()})
		return
	}
	defer db.Close()

	rows, err := db.Query("select * from cfx_accounts")
	if err != nil {
		context.JSON(500, gin.H{"error": err.Error()})
		return
	}
	defer rows.Close()

	for rows.Next() {
		var (
			id               string
			username         string
			fullname         string
			passwordHash     string
			passwordHashAlgo string
			email            string
			mfaEnabled       bool
			mfaType          string
			creationDate     time.Time
			phoneNumber      string
			active           bool
		)

		if err := rows.Scan(
			&id,
			&username,
			&fullname,
			&passwordHash,
			&passwordHashAlgo,
			&email,
			&mfaEnabled,
			&mfaType,
			&creationDate,
			&phoneNumber,
			&active,
		); err != nil {
			context.JSON(500, gin.H{"error": err.Error()})
			return
		}

		fmt.Println(id,
			username, fullname, passwordHash, passwordHashAlgo,
			email, mfaEnabled, mfaType, creationDate, phoneNumber, active)
	}

	if err := rows.Err(); err != nil {
		context.JSON(500, gin.H{"error": err.Error()})
	}
}
