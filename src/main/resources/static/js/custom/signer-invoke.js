/**
 * Este JS maneja la lógica de:
 * 1) Llamar al backend para generar el PDF en Base64.
 * 2) Invocar a la firma con "sign(...)" de autoscript.js.
 * 3) Enviar el PDF firmado al backend y mostrar enlace de descarga.
 * 4) NUEVO: Realizar cofirma de un PDF previamente firmado.
 */

// Variable global para recordar el ID (uuid) del PDF firmado
let globalSignedId = null;

/**
 * FIRMAR (primera firma del documento)
 */
function onClickFirmarAutorizacion() {
    showLoading();

    // 1) Recogemos datos del formulario
    const form = document.getElementById("form-authorization-data");
    const formData = new FormData(form);

    // Llamamos por AJAX a /signer/generate-pdf para obtener un PDF base64
    fetch("/vital-sanity/api/profesional-medico/generar-pdf-autorizacion", {
        method: "POST",
        body: formData
    })
        .then(response => response.text())
        .then(pdfBase64 => {
            // 2) Invocamos la firma con AutoFirma (sign):
            AutoScript.sign(
                pdfBase64,                  // dataB64
                "SHA512withRSA",            // algorithm
                "PAdES",                    // format
                null,                       // params (simple demo)
                function (signedPdfBase64, signerCert, extraInfo) {
                    // EXITO: subimos el PDF firmado al servidor
                    subirAutorizacionFirmada(signedPdfBase64);

                },
                function (errorType, errorMessage) {
                    alert("ERROR en firma: " + errorType + " - " + errorMessage);

                    hideLoading();
                }
            );
        })
        .catch(err => {
            alert("Error generando el PDF: " + err);
            hideLoading();
        });
}

/**
 * Subimos el PDF firmado (Base64) al servidor y mostramos enlace descarga.
 */
function subirAutorizacionFirmada(signedPdfBase64) {

    const formData = new FormData();
    formData.append("signedPdfBase64", signedPdfBase64);

    fetch("/vital-sanity/api/profesional-medico/pdf-autorizacion-firmada", {
        method: "POST",
        body: formData
    })
        .then(response => response.text())
        .then(uuid => {

            hideLoading();

            setTimeout(() => {
                window.location.href = "/vital-sanity/api/profesional-medico/pdf-autorizacion-firmada";
            }, 250);

        })
        .catch(err => {
            alert("Error subiendo PDF firmado: " + err);
            hideLoading();
        });
}



function onClickPacienteCofirmarAutorizacion() {
    showLoading();

    // Obtenemos el elemento del DOM con el id "id-solicitud-autorizacion"
    const solicitudElemento = document.getElementById("id-solicitud-autorizacion");
    if (!solicitudElemento) {
        console.error("Elemento con id 'id-solicitud-autorizacion' no encontrado.");
        hideLoading();
        return;
    }

    // Extraemos el valor del ID (suponiendo que el contenido es texto)
    const solicitudAutorizacionId = solicitudElemento.textContent.trim();

    // Construimos los parámetros en formato URL-encoded
    const params = new URLSearchParams();
    params.append("idSolicitudAutorizacion", solicitudAutorizacionId);

    const safeContextPath = typeof contextPath !== "undefined" ? contextPath : "";
    // Realizamos la llamada fetch enviando el parámetro
    fetch(`${safeContextPath}/api/paciente/solicitud-autorizacion-firmada`, {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: params.toString()
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("Error en la solicitud al servidor.");
            }
            return response.json();
        })
        .then(data => {
            const idSolicitud = data.idSolicitud;
            const signedPdfBase64 = data.pdfBase64;
            // console.log("Base64 recibido para cofirma:", signedPdfBase64);


            AutoScript.cosign(
                signedPdfBase64,          // firma ya existente en base64
                "SHA512withRSA",          // algoritmo
                "PAdES",                  // formato (PAdES)
                null,                     // params
                function (cosignedPdfBase64, signerCert, extraInfo) {
                    // EXITO: subimos la cofirma al servidor
                    subirAutorizacionCofirmada(idSolicitud, cosignedPdfBase64);

                    console.log("✔ Cofirma realizada correctamente. Resultado (Base64):", cosignedPdfBase64);
                },
                function (errorType, errorMessage) {
                    alert("ERROR en cofirma: " + errorType + " - " + errorMessage);

                    hideLoading();
                }
            );
        }) .catch(error => {
            console.error("Error al confirmar la autorización:", error);
            hideLoading();
        })
}


/**
 * Subimos el PDF de la autorizacion cofirmado (Base64) al servidor y mostramos enlace descarga.
 */
function subirAutorizacionCofirmada(idSolicitud, cosignedPdfBase64) {

    const formData = new FormData();
    formData.append("idSolicitudAutorizacion", idSolicitud);
    formData.append("cosignedPdfBase64", cosignedPdfBase64);


    fetch("/vital-sanity/api/paciente/aws-pdf-autorizacion-cofirmada", {
        method: "POST",
        body: formData
    })
        .then(response => response.text())
        .then(uuid => {

            hideLoading();

            setTimeout(() => {
                window.parent.location.href = `/vital-sanity/api/paciente/pdf-autorizacion-cofirmada?uuid=${uuid}`;
            }, 250);

        })
        .catch(err => {
            alert("Error subiendo PDF cofirmado: " + err);
            hideLoading();
        });
}

function onClickFirmarInforme() {
    showLoading();

    // 1) Recogemos datos del formulario
    const form = document.getElementById("form-authorization-data");
    const formData = new FormData(form);

    const safeContextPath = typeof contextPath !== "undefined" ? contextPath : "";

    // Llamamos por AJAX a /signer/generate-pdf para obtener un PDF base64
    fetch(`${safeContextPath}/api/profesional-medico/generar-pdf-informe`, {
        method: "POST",
        body: formData
    })
        .then(response => response.json())
        .then(data => {
            const informeId= data.idInforme;
            const pdfBase64 = data.pdfBase64;
            AutoScript.sign(
                pdfBase64,                  // dataB64
                "SHA512withRSA",            // algorithm
                "PAdES",                    // format
                null,                       // params (simple demo)
                function (signedPdfBase64, signerCert, extraInfo) {
                    // EXITO: subimos el PDF firmado al servidor
                    console.log("✔ Firma del informe realizada correctamente. Resultado (Base64):", signedPdfBase64);

                    subirPdfInformeFirmado(informeId, signedPdfBase64);

                },
                function (errorType, errorMessage) {
                    alert("ERROR en firma: " + errorType + " - " + errorMessage);

                    hideLoading();
                }
            );
        })
        .catch(err => {
            alert("Error generando el PDF: " + err);
            hideLoading();
        });
}

function subirPdfInformeFirmado(informeId, signedPdfBase64) {
    const payload = {
        idInforme: informeId,
        pdfBase64: signedPdfBase64
    };

    const safeContextPath = typeof contextPath !== "undefined" ? contextPath : "";

    fetch(`${safeContextPath}/api/profesional-medico/pdf-informe-firmado`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
    })
        .then(response => response.text())
        .then(uuid => {
            hideLoading();

            setTimeout(() => {
                window.parent.location.href = `/vital-sanity/api/profesional-medico/descargar-pdf-informe-firmado?uuid=${uuid}`;
            }, 250);
        })
        .catch(err => {
            alert("Error subiendo PDF firmado del informe: " + err);
            hideLoading();
        });
}


/**
 * Función para mostrar la pantalla de carga.
 */
function showLoading() {
    document.getElementById("loading-overlay").style.display = "flex";
}

/**
 * Función para ocultar la pantalla de carga.
 */
function hideLoading() {
    document.getElementById("loading-overlay").style.display = "none";
}


/**
 * IMPORTANTE (recordatorio de los servicios Storage/Retriever):
 * - Por defecto, AutoScript usará Socket si el navegador lo soporta.
 * - Si deseáramos forzar el uso de servicios, haríamos:
 *      setForceWSMode(true);
 *      setServlets("/vital-sanity/storage/StorageService","/vital-sanity/retriever/RetrieveService");
 */

// Al cargar la página, inicializamos la app de @firma:
window.addEventListener("load", () => {
    // Si se detecta un dispositivo móvil (Android o iOS), forzamos el uso de servicios intermedios
    // para garantizar la compatibilidad, y configuramos las URL de los servicios Storage y Retrieve.
    if (AutoScript.isAndroid() || AutoScript.isIOS()) {
        AutoScript.setForceWSMode(true);
        AutoScript.setServlets(
            "https://localhost/vital-sanity/afirma-signature-storage/StorageService",
            "https://localhost/vital-sanity/afirma-signature-retriever/RetrieveService"
        );
    }

    AutoScript.cargarAppAfirma();
});
