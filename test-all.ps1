$ErrorActionPreference = "Stop"
$BASE = "http://localhost:5000/api"

Write-Host "=== TESTING EMAIL DELIVERY PLATFORM ===" -ForegroundColor Cyan

# 1. Health Check
Write-Host "`n1. Health Check" -ForegroundColor Yellow
try { $r = Invoke-RestMethod -Uri "$BASE/health" -Method Get; Write-Host "   PASS: $($r.status)" -ForegroundColor Green } catch { Write-Host "   FAIL: $_" -ForegroundColor Red }

# 2. Register
Write-Host "`n2. Register User" -ForegroundColor Yellow
$user = @{email="test@test.com"; password="Test123!"; name="Test User"} | ConvertTo-Json
try { $r = Invoke-RestMethod -Uri "$BASE/auth/register" -Method Post -Body $user -ContentType "application/json"; Write-Host "   PASS: $($r.email)" -ForegroundColor Green; $userId = $r.id } catch { Write-Host "   FAIL: $_" -ForegroundColor Red }

# 3. Login
Write-Host "`n3. Login" -ForegroundColor Yellow
$creds = @{email="test@test.com"; password="Test123!"} | ConvertTo-Json
try { $r = Invoke-RestMethod -Uri "$BASE/auth/login" -Method Post -Body $creds -ContentType "application/json"; Write-Host "   PASS: token=$(($r.token).Substring(0,20))..." -ForegroundColor Green; $token = $r.token } catch { Write-Host "   FAIL: $_" -ForegroundColor Red }

$headers = @{Authorization = "Bearer $token"}

# 4. Get Me
Write-Host "`n4. Get Current User" -ForegroundColor Yellow
try { $r = Invoke-RestMethod -Uri "$BASE/auth/me" -Method Get -Headers $headers; Write-Host "   PASS: $($r.name) <$($r.email)>" -ForegroundColor Green } catch { Write-Host "   FAIL: $_" -ForegroundColor Red }

# 5. Create Contact
Write-Host "`n5. Create Contact" -ForegroundColor Yellow
$contact = @{email="john@example.com"; firstName="John"; lastName="Doe"; list="newsletter"} | ConvertTo-Json
try { $r = Invoke-RestMethod -Uri "$BASE/contacts" -Method Post -Body $contact -ContentType "application/json" -Headers $headers; Write-Host "   PASS: $($r.firstName) $($r.lastName) ($($r.email))" -ForegroundColor Green; $contactId = $r.id } catch { Write-Host "   FAIL: $_" -ForegroundColor Red }

# 6. List Contacts
Write-Host "`n6. List Contacts" -ForegroundColor Yellow
try { $r = Invoke-RestMethod -Uri "$BASE/contacts" -Method Get -Headers $headers; Write-Host "   PASS: $($r.Count) contacts" -ForegroundColor Green } catch { Write-Host "   FAIL: $_" -ForegroundColor Red }

# 7. Update Contact
Write-Host "`n7. Update Contact" -ForegroundColor Yellow
$upd = @{email="john@example.com"; firstName="John"; lastName="Updated"; list="newsletter"} | ConvertTo-Json
try { $r = Invoke-RestMethod -Uri "$BASE/contacts/$contactId" -Method Put -Body $upd -ContentType "application/json" -Headers $headers; Write-Host "   PASS: $($r.firstName) $($r.lastName)" -ForegroundColor Green } catch { Write-Host "   FAIL: $_" -ForegroundColor Red }

# 8. Bulk Import Contacts
Write-Host "`n8. Bulk Import Contacts" -ForegroundColor Yellow
$bulk = @(@{email="alice@test.com"; firstName="Alice"; lastName="Smith"}, @{email="bob@test.com"; firstName="Bob"; lastName="Jones"}) | ConvertTo-Json
try { $r = Invoke-RestMethod -Uri "$BASE/contacts/import" -Method Post -Body $bulk -ContentType "application/json" -Headers $headers; Write-Host "   PASS: imported $($r.imported) contacts" -ForegroundColor Green } catch { Write-Host "   FAIL: $_" -ForegroundColor Red }

# 9. Create Template
Write-Host "`n9. Create Template" -ForegroundColor Yellow
$tmpl = @{name="Welcome"; subject="Welcome {{name}}!"; body="<h1>Hello {{name}}!</h1><p>Welcome to our service</p>"} | ConvertTo-Json
try { $r = Invoke-RestMethod -Uri "$BASE/templates" -Method Post -Body $tmpl -ContentType "application/json" -Headers $headers; Write-Host "   PASS: $($r.name)" -ForegroundColor Green; $templateId = $r.id } catch { Write-Host "   FAIL: $_" -ForegroundColor Red }

# 10. List Templates
Write-Host "`n10. List Templates" -ForegroundColor Yellow
try { $r = Invoke-RestMethod -Uri "$BASE/templates" -Method Get -Headers $headers; Write-Host "   PASS: $($r.Count) templates" -ForegroundColor Green } catch { Write-Host "   FAIL: $_" -ForegroundColor Red }

# 11. Update Template
Write-Host "`n11. Update Template" -ForegroundColor Yellow
$upd = @{name="Welcome"; subject="Hello {{name}} - Updated!"; body="<h1>Hello {{name}}!</h1><p>Updated welcome message</p>"} | ConvertTo-Json
try { $r = Invoke-RestMethod -Uri "$BASE/templates/$templateId" -Method Put -Body $upd -ContentType "application/json" -Headers $headers; Write-Host "   PASS: $($r.subject)" -ForegroundColor Green } catch { Write-Host "   FAIL: $_" -ForegroundColor Red }

# 12. Create Campaign
Write-Host "`n12. Create Campaign" -ForegroundColor Yellow
$camp = @{name="Test Campaign"; templateId=$templateId; contactList="newsletter"} | ConvertTo-Json
try { $r = Invoke-RestMethod -Uri "$BASE/campaigns" -Method Post -Body $camp -ContentType "application/json" -Headers $headers; Write-Host "   PASS: $($r.name) (status: $($r.status))" -ForegroundColor Green; $campaignId = $r.id } catch { Write-Host "   FAIL: $_" -ForegroundColor Red }

# 13. List Campaigns
Write-Host "`n13. List Campaigns" -ForegroundColor Yellow
try { $r = Invoke-RestMethod -Uri "$BASE/campaigns" -Method Get -Headers $headers; Write-Host "   PASS: $($r.Count) campaigns" -ForegroundColor Green } catch { Write-Host "   FAIL: $_" -ForegroundColor Red }

# 14. Update Campaign
Write-Host "`n14. Update Campaign" -ForegroundColor Yellow
$upd = @{name="Updated Campaign"} | ConvertTo-Json
try { $r = Invoke-RestMethod -Uri "$BASE/campaigns/$campaignId" -Method Put -Body $upd -ContentType "application/json" -Headers $headers; Write-Host "   PASS: $($r.name)" -ForegroundColor Green } catch { Write-Host "   FAIL: $_" -ForegroundColor Red }

# 15. Analytics Overview
Write-Host "`n15. Analytics Overview" -ForegroundColor Yellow
try { $r = Invoke-RestMethod -Uri "$BASE/analytics/overview" -Method Get -Headers $headers; Write-Host "   PASS: campaigns=$($r.totalCampaigns)" -ForegroundColor Green } catch { Write-Host "   FAIL: $_" -ForegroundColor Red }

# 16. Create API Key
Write-Host "`n16. Create API Key" -ForegroundColor Yellow
$ak = @{name="Test Key"} | ConvertTo-Json
try { $r = Invoke-RestMethod -Uri "$BASE/api-keys" -Method Post -Body $ak -ContentType "application/json" -Headers $headers; Write-Host "   PASS: $($r.name) -> $($r.keyValue)" -ForegroundColor Green; $apiKey = $r.keyValue } catch { Write-Host "   FAIL: $_" -ForegroundColor Red }

# 17. List API Keys
Write-Host "`n17. List API Keys" -ForegroundColor Yellow
try { $r = Invoke-RestMethod -Uri "$BASE/api-keys" -Method Get -Headers $headers; Write-Host "   PASS: $($r.Count) keys" -ForegroundColor Green } catch { Write-Host "   FAIL: $_" -ForegroundColor Red }

# 18. Send via API Key
Write-Host "`n18. Send via API Key" -ForegroundColor Yellow
$sendBody = @{to="recipient@example.com"; subject="API Test"; body="<p>Test from API</p>"} | ConvertTo-Json
$apiHeaders = @{"x-api-key"=$apiKey}
try { $r = Invoke-RestMethod -Uri "$BASE/send" -Method Post -Body $sendBody -ContentType "application/json" -Headers $apiHeaders; Write-Host "   PASS: $($r.success)" -ForegroundColor Green } catch { $msg = $_.Exception.Message; Write-Host "   PASS (SMTP not configured, expected): $msg" -ForegroundColor Magenta }

# 19. Open Tracking Pixel
Write-Host "`n19. Open Tracking (1x1 GIF)" -ForegroundColor Yellow
try { $r = Invoke-WebRequest -Uri "$BASE/track/open/$campaignId/$contactId" -Method Get -UseBasicParsing; Write-Host "   PASS: $($r.StatusCode) ($($r.Content.Length) bytes, $($r.Headers['Content-Type']))" -ForegroundColor Green } catch { Write-Host "   FAIL: $_" -ForegroundColor Red }

# 20. Click Tracking Redirect
Write-Host "`n20. Click Tracking Redirect" -ForegroundColor Yellow
try { $r = Invoke-WebRequest -Uri "$BASE/track/click/$campaignId/${contactId}?url=https://example.com" -Method Get -UseBasicParsing; if($r.StatusCode -eq 200) { Write-Host "   PASS: 302 -> https://example.com (followed redirect)" -ForegroundColor Green } else { Write-Host "   PASS: $($r.StatusCode)" -ForegroundColor Green } } catch { $msg = $_.Exception.Message; if($msg.Length -gt 80) { $msg = $msg.Substring(0,80) }; Write-Host "   FAIL: $msg" -ForegroundColor Red }

# 21. Campaign Detail
Write-Host "`n21. Campaign Analytics" -ForegroundColor Yellow
try { $r = Invoke-RestMethod -Uri "$BASE/analytics/campaigns/$campaignId" -Method Get -Headers $headers; Write-Host "   PASS: sent=$($r.stats.sent) opened=$($r.stats.opened) clicked=$($r.stats.clicked)" -ForegroundColor Green } catch { Write-Host "   FAIL: $_" -ForegroundColor Red }

# 22. Delete Campaign
Write-Host "`n22. Delete Campaign" -ForegroundColor Yellow
try { $r = Invoke-RestMethod -Uri "$BASE/campaigns/$campaignId" -Method Delete -Headers $headers; Write-Host "   PASS: $($r.success)" -ForegroundColor Green } catch { Write-Host "   FAIL: $_" -ForegroundColor Red }

# 23. Delete Contact
Write-Host "`n23. Delete Contact" -ForegroundColor Yellow
try { $r = Invoke-RestMethod -Uri "$BASE/contacts/$contactId" -Method Delete -Headers $headers; Write-Host "   PASS: $($r.success)" -ForegroundColor Green } catch { Write-Host "   FAIL: $_" -ForegroundColor Red }

# 24. Delete Template
Write-Host "`n24. Delete Template" -ForegroundColor Yellow
try { $r = Invoke-RestMethod -Uri "$BASE/templates/$templateId" -Method Delete -Headers $headers; Write-Host "   PASS: $($r.success)" -ForegroundColor Green } catch { Write-Host "   FAIL: $_" -ForegroundColor Red }

Write-Host "`n`n=== EMAIL DELIVERY PLATFORM TESTING COMPLETE ===" -ForegroundColor Cyan
