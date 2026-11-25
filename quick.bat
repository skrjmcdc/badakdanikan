@echo off
java parser < %1 > temp && java Machine temp && del temp