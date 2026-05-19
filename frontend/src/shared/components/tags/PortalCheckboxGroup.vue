<script setup lang="ts">
export type PortalCheckOption = {
  label: string;
  value: string;
  disabled?: boolean;
};

const model = defineModel<string[]>({ default: () => [] });

withDefaults(defineProps<{
  label?: string;
  options: PortalCheckOption[];
  disabled?: boolean;
  inline?: boolean;
}>(), {
  label: "",
  disabled: false,
  inline: true,
});

function toggle(value: string, checked: boolean) {
  if (checked) {
    model.value = Array.from(new Set([...model.value, value]));
    return;
  }
  model.value = model.value.filter((item) => item !== value);
}
</script>

<template>
  <fieldset class="portal-field">
    <legend v-if="label" class="portal-field-label">{{ label }}</legend>
    <div class="portal-choice-group" :class="{ 'portal-choice-group-stacked': !inline }">
      <label v-for="option in options" :key="option.value" class="portal-choice">
        <input
          type="checkbox"
          :checked="model.includes(option.value)"
          :disabled="disabled || option.disabled"
          @change="toggle(option.value, ($event.target as HTMLInputElement).checked)"
        />
        <span>{{ option.label }}</span>
      </label>
    </div>
  </fieldset>
</template>
