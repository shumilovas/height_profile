import rasterio
import csv
import numpy as np
import os
import pandas as pd
import openpyxl


def read_tiff_info(file_path):
    with rasterio.open(file_path) as src:
        bounds = src.bounds
        data = src.read(1)
        transform = src.transform

        info = {
            'width': src.width,
            'height': src.height,
            'bounds': bounds,
            'min_x': bounds.left,
            'max_x': bounds.right,
            'min_y': bounds.bottom,
            'max_y': bounds.top,
            'min_height': float(data.min()),
            'max_height': float(data.max()),
            'transform': transform,
            'data': data.copy()
        }

        return info


def get_user_coordinates(info):
    print("\n" + "=" * 50)
    print("ВВЕДИТЕ КООРДИНАТЫ ДВУХ ТОЧЕК")
    print("=" * 50)

    print(f"\nДиапазон координат файла:")
    print(f"X: от {info['min_x']:.1f} до {info['max_x']:.1f}")
    print(f"Y: от {info['min_y']:.1f} до {info['max_y']:.1f}")

    print(f"\n--- НАЧАЛЬНАЯ ТОЧКА ---")
    while True:
        try:
            start_x = float(input(f"X координата: "))
            if info['min_x'] <= start_x <= info['max_x']:
                break
            else:
                print(f"Ошибка: X должен быть между {info['min_x']:.1f} и {info['max_x']:.1f}")
        except ValueError:
            print("Ошибка: введите число")

    while True:
        try:
            start_y = float(input(f"Y координата: "))
            if info['min_y'] <= start_y <= info['max_y']:
                break
            else:
                print(f"Ошибка: Y должен быть между {info['min_y']:.1f} и {info['max_y']:.1f}")
        except ValueError:
            print("Ошибка: введите число")

    print(f"\n--- КОНЕЧНАЯ ТОЧКА ---")
    while True:
        try:
            end_x = float(input(f"X координата: "))
            if info['min_x'] <= end_x <= info['max_x']:
                break
            else:
                print(f"Ошибка: X должен быть между {info['min_x']:.1f} и {info['max_x']:.1f}")
        except ValueError:
            print("Ошибка: введите число")

    while True:
        try:
            end_y = float(input(f"Y координата: "))
            if info['min_y'] <= end_y <= info['max_y']:
                break
            else:
                print(f"Ошибка: Y должен быть между {info['min_y']:.1f} и {info['max_y']:.1f}")
        except ValueError:
            print("Ошибка: введите число")

    return (start_x, start_y), (end_x, end_y)


def calculate_profile(info, start_point, end_point, num_points=100):
    data = info['data']
    transform = info['transform']

    start_x, start_y = start_point
    end_x, end_y = end_point

    x_coords = np.linspace(start_x, end_x, num_points)
    y_coords = np.linspace(start_y, end_y, num_points)

    profile = []
    total_distance = 0.0

    for i in range(num_points):
        x = x_coords[i]
        y = y_coords[i]

        row, col = rasterio.transform.rowcol(transform, x, y)

        if 0 <= row < data.shape[0] and 0 <= col < data.shape[1]:
            height = float(data[row, col])

            if i > 0:
                prev_x = x_coords[i - 1]
                prev_y = y_coords[i - 1]
                dx = x - prev_x
                dy = y - prev_y
                segment = np.sqrt(dx * dx + dy * dy)
                total_distance += segment
            else:
                segment = 0.0

            point_data = {
                'index': i + 1,
                'x': float(x),
                'y': float(y),
                'height': height,
                'distance': float(total_distance),
                'segment': float(segment)
            }
            profile.append(point_data)
        else:
            print(f"Точка {i + 1} ({x:.1f}, {y:.1f}) вне границ данных")

    return profile


def save_to_csv(profile, filename="height_profile.csv"):
    with open(filename, 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f)

        writer.writerow(['X', 'Y', 'Height'])

        for point in profile:
            writer.writerow([
                f"{point['x']:.3f}",
                f"{point['y']:.3f}",
                f"{point['height']:.3f}"
            ])

    return len(profile)


def print_statistics(profile):
    if not profile:
        print("Нет данных для статистики")
        return

    heights = [p['height'] for p in profile]

    print("\n" + "=" * 50)
    print("СТАТИСТИКА ПРОФИЛЯ")
    print("=" * 50)
    print(f"Количество точек: {len(profile)}")
    print(f"Длина профиля: {profile[-1]['distance']:.1f} м")
    print(f"Минимальная высота: {min(heights):.1f} м")
    print(f"Максимальная высота: {max(heights):.1f} м")
    print(f"Средняя высота: {np.mean(heights):.1f} м")
    print(f"Перепад высот: {max(heights) - min(heights):.1f} м")


def main():
    print("=" * 60)
    print("ГЕНЕРАТОР ПРОФИЛЯ ВЫСОТ ИЗ TIF ФАЙЛА")
    print("=" * 60)

    tif_file = "out.tif"
    if not os.path.exists(tif_file):
        print(f"ОШИБКА: Файл '{tif_file}' не найден!")
        print("Поместите файл в ту же папку, что и скрипт")
        input("Нажмите Enter для выхода...")
        return

    print(f"\nЧтение файла: {tif_file}")
    try:
        info = read_tiff_info(tif_file)
        print("Файл успешно прочитан")
        print(f"Размер: {info['width']} x {info['height']} пикселей")
        print(f"Диапазон высот: {info['min_height']:.1f} - {info['max_height']:.1f} м")
    except Exception as e:
        print(f"ОШИБКА при чтении файла: {e}")
        input("Нажмите Enter для выхода...")
        return

    start_point, end_point = get_user_coordinates(info)

    print(f"\n--- НАСТРОЙКИ ПРОФИЛЯ ---")
    while True:
        try:
            num_points = int(input("Количество точек на профиле (рекомендуется 50-200): "))
            if num_points >= 2:
                break
            else:
                print("Ошибка: должно быть минимум 2 точки")
        except ValueError:
            print("Ошибка: введите целое число")

    print(f"\nВычисление профиля...")
    profile = calculate_profile(info, start_point, end_point, num_points)

    if not profile:
        print("ОШИБКА: Не удалось получить данные профиля")
        print("Возможно, точки находятся вне зоны данных")
        input("Нажмите Enter для выхода...")
        return

    print(f"Получено точек: {len(profile)}")

    output_file = "height_profile_csv.csv"
    points_saved = save_to_csv(profile, output_file)
    df = pd.DataFrame(profile)
    # 3. Сохраняем в Excel
    # index=False убирает колонку с индексами из файла Excel
    df.to_excel('height_profile_xl.xlsx', index=False, sheet_name='height_profile')
    print("Таблица сохранена в файл height_profile.xlsx")
    print(f"Данные сохранены в файл: {output_file}")

    print_statistics(profile)

    print(f"\nПервые 5 точек профиля:")
    print("-" * 50)
    print(f"{'№':<4} {'X':<12} {'Y':<12} {'Высота':<8}")
    print("-" * 50)

    for i in range(min(5, len(profile))):
        p = profile[i]
        print(f"{p['index']:<4} {p['x']:<12.1f} {p['y']:<12.1f} {p['height']:<8.1f}")

    print("\n" + "=" * 60)
    print(f"ГОТОВО! CSV файл создан: {output_file}")
    print(f"Всего точек: {points_saved}")
    print("=" * 60)

    input("\nНажмите Enter для выхода...")


if __name__ == "__main__":
    main()