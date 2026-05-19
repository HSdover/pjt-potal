<script setup lang="ts">
export type PortalRadioOption = {
  label: string;
  value: string;
  disabled?: boolean;
};

const model = defineModel<string>({ default: "" });

withDefaults(defineProps<{
  label?: string;
  name: string;
  options: PortalRadioOption[];
  disabled?: boolean;
  inline?: boolean;
}>(), {
  label: "",
  disabled: false,
  inline: true,
});
</script>

<template>
  <fieldset class="portal-field">
    <legend v-if="label" class="portal-field-label">{{ label }}</legend>
    <div class="portal-choice-group" :class="{ 'portal-choice-group-stacked': !inline }">
      <label v-for="option in options" :key="option.value" class="portal-choice">
        <input
          v-model="model"
          type="radio"
          :name="name"
          :value="option.value"
          :disabled="disabled || option.disabled"
        />
        <span>{{ option.label }}</span>
      </label>
    </div>
  </fieldset>
</template>
