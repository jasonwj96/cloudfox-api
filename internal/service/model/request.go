package model

type LoginRequest struct {
	AccountID string `json:"accountId" binding:"required,uuid"`
}
