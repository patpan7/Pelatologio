<?php
include 'db.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $afm = $_POST['afm'];

    $sql = "SELECT code FROM Customers WHERE afm = ?";
    $params = array($afm);
    $stmt = sqlsrv_query($conn, $sql, $params);

    if ($row = sqlsrv_fetch_array($stmt, SQLSRV_FETCH_ASSOC)) {
        session_start();
        $_SESSION['afm'] = $afm;
        header("Location: dashboard.php");
        exit;
    } else {
        $error = "Το ΑΦΜ δεν βρέθηκε!";
    }
}
?>
<!DOCTYPE html>
<html lang="el">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Portal Πελατών</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="style.css">
</head>
<body class="d-flex justify-content-center align-items-center vh-100 bg-light">
    <div class="card p-4 shadow-lg" style="width: 350px;">
        <h3 class="text-center">Σύνδεση στο Portal</h3>
        <form method="post">
            <div class="mb-3">
                <label for="afm" class="form-label">ΑΦΜ:</label>
                <input type="text" name="afm" id="afm" class="form-control" required>
            </div>
            <button type="submit" class="btn btn-primary w-100">Σύνδεση</button>
        </form>
        <?php if (isset($error)) echo "<p class='text-danger text-center mt-2'>$error</p>"; ?>
    </div>
</body>
</html>
