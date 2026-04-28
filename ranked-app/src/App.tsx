import React, { useState } from 'react';
import {DndContext, closestCenter, KeyboardSensor, PointerSensor, useSensor, useSensors, type DragEndEvent} from '@dnd-kit/core';
import { SortableContext, sortableKeyboardCoordinates, verticalListSortingStrategy } from '@dnd-kit/sortable';
import { SortableItem } from './SortableItem';

// Source: https://www.xjavascript.com/blog/dndkit-typescript/#sortable-lists

const items = ['Item 1', 'Item 2', 'Item 3'];

const App: React.FC = () => {
    const [listItems, setListItems] = useState(items);

    const sensors = useSensors(
        useSensor(PointerSensor),
        useSensor(KeyboardSensor, {
            coordinateGetter: sortableKeyboardCoordinates,
        })
    );

    const handleDragEnd = ({ active, over }: DragEndEvent) => {
        if (!over) return;

        if (active.id !== over.id) {
            setListItems((items) => {
                const oldIndex = items.indexOf(active.id as string);
                const newIndex = items.indexOf(over.id as string);

                const newItems = [...items];
                newItems.splice(oldIndex, 1);
                newItems.splice(newIndex, 0, active.id as string);

                return newItems;
            });
        }
    };

    return (
        <DndContext
            sensors={sensors}
            collisionDetection={closestCenter}
            onDragEnd={handleDragEnd}
        >
            <SortableContext
                items={listItems}
                strategy={verticalListSortingStrategy}
            >
                <div>
                    {listItems.map((item, index) => (
                        <SortableItem key={index} id={item} />
                    ))}
                </div>
            </SortableContext>
        </DndContext>
    );
};

export default App;