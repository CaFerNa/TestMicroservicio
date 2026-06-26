// wasm-api.js
var API_ENDPOINT = 'https://servicesws.csic.es/services/ProyectoDSS/proyectos/desde/1/hasta/';

const WasmAPI = {
    teavm: null,

    // 1. INICIALIZACIÓN
    init: async function(wasmPath) {
        console.log("Cargando Wasm...");
        this.teavm = await TeaVM.wasm.load(wasmPath);

        // Ejecutar el main de Java para inicializar estáticos
        if (this.teavm.main) {
            await this.teavm.main([]);
        }
        if (!this.teavm) return "Error: Wasm no cargado";
        console.log("Wasm cargado.");
    },

    // 2. FUNCIONES PÚBLICAS

    getHTML: async function(rows) {
            var xmlData = "";
            var consulta = API_ENDPOINT + rows;
            try {
                    const response = await fetch(consulta);
                    if (!response.ok) {
                        throw new Error(`HTTP error! status: ${response.status}`);
                    }
                    xmlData= await response.text();
                    console.log('Data fetched. Rendering with WASM...');
                } catch (error) {
                    console.error('Error:', error);
                    return "<div class='empty'>" + error + "</div>";
                }
            const result = this.getHTMLFromXML(xmlData);
            return result;
    },

    getHTMLFromXML: function(datos) {
        // 1. Forzar a que sea string (por si acaso llega como objeto o número)
        const str = String(datos);
        const length = str.length;
        const intArrayPtr = this.teavm.allocateIntArray(length);
        const wasmMemoryView = new Int32Array(
            this.teavm.memory.buffer,
            this.teavm.intArrayData(intArrayPtr),
            length
        );
        for (let i = 0; i < length; i++) {
            wasmMemoryView[i] = str.charCodeAt(i);
        }
        console.log('Array copiado. Ejecutando WASM...');
        const ptrSalida = this.teavm.instance.exports.getHTML(intArrayPtr);
        const generatedHtml = this._wasmToJs(ptrSalida);
        return generatedHtml;
    },

    // 3. HERRAMIENTAS INTERNAS (PRIVADAS)
    // Estas son las que copias una vez y te olvidas de ellas para siempre

    _jsToWasm: function(jsString) {
        const ptr = this.teavm.allocateString(jsString.length);
        const memory = new Uint16Array(this.teavm.memory.buffer);
        const dataPtr = this.teavm.stringData(ptr);
        const arrayDataPtr = this.teavm.charArrayData(dataPtr);
        for (let i = 0; i < jsString.length; ++i) {
            memory[(arrayDataPtr / 2) + i] = jsString.charCodeAt(i);
        }
        return ptr;
    },

    _wasmToJs: function(ptr) {
        if (ptr === 0) return "";
        const memory = new Uint16Array(this.teavm.memory.buffer);
        const dataPtr = this.teavm.stringData(ptr);
        const arrayDataPtr = this.teavm.charArrayData(dataPtr);
        const length = this.teavm.arrayLength(dataPtr);
        let result = "";
        for (let i = 0; i < length; ++i) {
            result += String.fromCharCode(memory[(arrayDataPtr / 2) + i]);
        }
        return result;
    }
};
