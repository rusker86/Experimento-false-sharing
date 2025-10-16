import subprocess
import threading
import tkinter as tk
from tkinter import ttk
import matplotlib.pyplot as plt
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg
import signal

# Lista global de procesos Java activos
java_processes = []
stop_flag = False  # bandera para indicar cierre

# Función segura para actualizar etiquetas de Tkinter desde cualquier hilo
def safe_update_label(label, text):
	if label.winfo_exists():
		label.config(text=text)
		root.update_idletasks()

def run_java(mode):
	"""Ejecuta el benchmark Java y devuelve el tiempo total."""
	cmd = ["java", "Main"] + ([mode] if mode else [])
	process = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
	java_processes.append(process)

	result = None
	try:
		for line in process.stdout:
			if stop_flag:  # detener proceso si se cierra ventana
				process.send_signal(signal.SIGTERM)
				break
			if line.startswith("GLOBAL"):
				_, _, time_ms = line.strip().split("\t")
				result = float(time_ms)
	except Exception:
		pass
	finally:
		if process in java_processes:
			java_processes.remove(process)
		process.stdout.close()
		process.stderr.close()

	return result

def run_experiment():
	"""Ejecuta las pruebas Java y dibuja el gráfico de manera segura en Tkinter."""
	safe_update_label(status_label, "Ejecutando experimento, por favor espera...")

	# Compilar Java
	subprocess.run(["javac", "Main.java"], check=True)

	def worker():
		global stop_flag
		stop_flag = False

		t_false = run_java("")       # con false sharing
		t_true = run_java("no-fs")   # sin false sharing

		if stop_flag:  # detener actualización si cerraron ventana
			return

		# Preparar gráfico
		fig, ax = plt.subplots(figsize=(4, 3))
		ax.bar(["Con False Sharing", "Sin False Sharing"], [t_false, t_true], color=["red", "green"])
		ax.set_ylabel("Tiempo total (ms)")
		ax.set_title("Comparación de rendimiento", pad = 10)

		for i, v in enumerate([t_false, t_true]):
			ax.text(i, v + (v * 0.05), f"{v:.0f} ms", ha="center")

		explanation = (
			"💡 *False Sharing* ocurre cuando varios hilos escriben en datos cercanos en memoria.\n"
			"Aunque sean variables distintas, comparten la misma línea de caché y el procesador\n"
			"tiene que invalidar y actualizar copias constantemente, provocando ralentizaciones."
		)

		# Dibujar el gráfico y actualizar etiquetas desde el hilo principal
		def update_gui():
			if root.winfo_exists():
				canvas = FigureCanvasTkAgg(fig, master=root)
				canvas.get_tk_widget().grid(row=3, column=0, pady=10)
				canvas.draw()
				text_label.config(text=explanation)
				status_label.config(text="Listo ✅")

		root.after(0, update_gui)

	# Ejecutar worker en un hilo separado para no bloquear la GUI
	threading.Thread(target=worker, daemon=True).start()

def on_close():
	"""Detiene procesos Java al cerrar la ventana."""
	global stop_flag
	stop_flag = True
	for p in java_processes:
		try:
			p.send_signal(signal.SIGTERM)
		except Exception:
			pass
	root.destroy()

# -------------------
# Configuración GUI
# -------------------
root = tk.Tk()
root.title("Simulador educativo: False Sharing en Java")
root.protocol("WM_DELETE_WINDOW", on_close)

title = ttk.Label(root, text="Experimento: False Sharing en la JVM", font=("Segoe UI", 14, "bold"))
title.grid(row=0, column=0, pady=10)

button = ttk.Button(root, text="▶ Ejecutar experimento", command=run_experiment)
button.grid(row=1, column=0, pady=10)

status_label = ttk.Label(root, text="")
status_label.grid(row=2, column=0)

text_label = ttk.Label(root, text="", justify="center")
text_label.grid(row=4, column=0, pady=10)

root.mainloop()
