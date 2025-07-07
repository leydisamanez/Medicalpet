function calcularTotal() {
    const productoSelect = document.getElementById("productoSelect");
    const cantidadInput = document.getElementById("cantidad");
    const descuentoInput = document.getElementById("descuento");
    const totalEstimado = document.getElementById("totalEstimado");

    if (productoSelect && cantidadInput && descuentoInput && totalEstimado) {
        const precio = parseFloat(productoSelect.options[productoSelect.selectedIndex].getAttribute("data-precio")) || 0;
        const cantidad = parseInt(cantidadInput.value) || 0;
        const descuento = parseFloat(descuentoInput.value) || 0;

        const subtotal = precio * cantidad;
        const totalDescuento = subtotal * (descuento / 100);
        const total = subtotal - totalDescuento;

        totalEstimado.value = `$${total.toFixed(2)}`;
    }
}