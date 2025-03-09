<!DOCTYPE html>
<html lang="el">
<head>
    <meta charset="UTF-8">
    <title>Τραπεζικοί Λογαριασμοί</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css">
    <link rel="stylesheet" href="style.css">

</head>
<body>
    <?php include 'navbar.php'; ?>

    <div class="container mt-5">
        <h2 class="text-center mb-4"><i class="fas fa-university"></i> Τραπεζικοί Λογαριασμοί</h2>

        <div class="row">
            <!-- Εθνική Τράπεζα -->
        <div class="col-md-4 d-flex mb-4">
            <div class="card bank-card shadow-lg h-100 w-100">
                <div class="card-header bg-success text-white bank-header">
                        <img src="images/ethniki.png" alt="Εθνική Τράπεζα">
                        <h5 class="mb-0">Εθνική Τράπεζα</h5>
                    </div>
                    <div class="card-body">
                        <p><strong>Αριθμός Λογαριασμού:</strong> 29700119679</p>
                        <p><strong>IBAN:</strong> GR6201102970000029700119679</p>
                        <p><strong>BIC / SWIFT:</strong> ETHNGRAA</p>
                        <p><strong>Δικαιούχος:</strong> ΓΚΟΥΜΑΣ ΔΗΜΗΤΡΙΟΣ ΑΠΟΣΤΟΛΟΣ</p>
                    </div>
                </div>
            </div>

            <!-- Eurobank -->
        <div class="col-md-4 d-flex mb-4">
            <div class="card bank-card shadow-lg h-100 w-100">
                    <div class="card-header text-white bank-header" style="background: linear-gradient(to right, blue, red);">
                        <img src="images/eurobank.png" alt="Eurobank">
                        <h5 class="mb-0">Eurobank</h5>
                    </div>
                    <div class="card-body">
                        <p><strong>Αριθμός Λογαριασμού:</strong> 0026.0451.27.0200083481</p>
                        <p><strong>IBAN:</strong> GR7902604510000270200083481</p>
                        <p><strong>BIC / SWIFT:</strong> ERBKGRAA</p>
                        <p><strong>Δικαιούχος:</strong> ΓΚΟΥΜΑΣ ΔΗΜΗΤΡΙΟΣ ΑΠΟΣΤΟΛΟΣ</p>
                    </div>
                </div>
            </div>

            <!-- myPOS -->
        <div class="col-md-4 d-flex mb-4">
            <div class="card bank-card shadow-lg h-100 w-100">
                    <div class="card-header bg-primary text-white bank-header">
                        <img src="images/mypos.png" alt="myPOS">
                        <h5 class="mb-0">myPOS</h5>
                    </div>
                    <div class="card-body">
                        <p><strong>Αριθμός Λογαριασμού:</strong> 40005794314</p>
                        <p><strong>IBAN:</strong> IE27MPOS99039012868261</p>
                        <p><strong>BIC / SWIFT:</strong> MPOSIE2D</p>
                        <p><strong>Δικαιούχος:</strong> GKOUMAS DIMITRIOS</p>
                    </div>
                </div>
            </div>
        </div>

        <!-- Κουμπί επιστροφής -->
        <div class="text-center mt-4">
            <a href="dashboard.php" class="btn btn-secondary"><i class="fas fa-arrow-left"></i> Επιστροφή</a>
        </div>
    </div>
</body>
</html>
