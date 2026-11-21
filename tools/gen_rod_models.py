#!/usr/bin/env python3
"""Конструктор удочки: собирает модели видов удочки из слоёв-деталей.

Детали лежат в assets/newfishing/textures/item/fishing_rod_<деталь>_part-sprite.png (16x16, инвентарь) и
fishing_rod_<деталь>_part-model.png (32x32, в руке). Каждый вид удочки = тело (empty) + леска + крючок, наложенные слоями.
Запуск после добавления или переименования деталей:  python tools/gen_rod_models.py
Новый вид: добавить строку в STATES (имя файла модели, леска, крючок); предикат rod_state в fishing_rod.json уже указывает на модель.
"""
import json
import os

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src', 'main', 'resources', 'assets', 'newfishing', 'models', 'item')

# имя модели -> (леска, крючок); None = нет детали
STATES = {
    'empty': (None, None),
    'line': ('line', None), 'full': ('line', 'float'), 'lava_float': ('line', 'lava_float'),
    'line_and_ender': ('line', 'ender-float'),
    'with_carrot': ('line', 'with_carrot'), 'with_warped_fungus': ('line', 'with_warped_fungus'),
    'netherite_line': ('netherite_line', None), 'netherite_line_float': ('netherite_line', 'float'),
    'netherite_full': ('netherite_line', 'lava_float'), 'netherite_line_ender': ('netherite_line', 'ender-float'),
    'netherite_with_carrot': ('netherite_line', 'with_carrot'),
    'netherite_with_warped_fungus': ('netherite_line', 'with_warped_fungus'),
    'phantom_line': ('phantom_line', None), 'phantom_and_float': ('phantom_line', 'float'),
    'phantom_and_netherite': ('phantom_line', 'lava_float'), 'phantom_and_ender': ('phantom_line', 'ender-float'),
    'phantom_with_carrot': ('phantom_line', 'with_carrot'),
    'phantom_with_warped_fungus': ('phantom_line', 'with_warped_fungus'),
    # закинутая удочка: только леска (поплавок в воде)
    'used': ('line_used', None), 'netherite_used': ('netherite_line_used', None), 'phantom_used': ('phantom_line_used', None),
}


def layers(kind, line, hook):
    parts = ['empty'] + [p for p in (line, hook) if p]
    return {f'layer{i}': f'newfishing:item/fishing_rod_{p}_part-{kind}' for i, p in enumerate(parts)}


def main():
    for name, (line, hook) in STATES.items():
        sprite = layers('sprite', line, hook)
        model = layers('model', line, hook)
        model['particle'] = 'newfishing:item/fishing_rod_empty_part-sprite'
        data = {
            'loader': 'neoforge:separate_transforms',
            'gui_light': 'front',
            'base': {'parent': 'newfishing:item/rod_hand', 'textures': model},
            'perspectives': {k: {'parent': 'minecraft:item/generated', 'textures': dict(sprite)} for k in ('gui', 'ground', 'fixed')},
        }
        with open(os.path.join(ROOT, f'fishing_rod_{name}.json'), 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=2)
            f.write('\n')
    print(len(STATES), 'моделей записано')


if __name__ == '__main__':
    main()
