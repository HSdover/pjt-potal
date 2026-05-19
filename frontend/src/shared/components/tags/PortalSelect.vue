<script setup lang="ts">
export type PortalSelectOption = {
  label: string;
  value: string;
  disabled?: boolean;
};

const model = defineModel<string>({ default: "" });

withDefaults(defineProps<{
  id?: string;
  label?: string;
  placeholder?: string;
  disabled?: boolean;
  options: PortalSelectOption[];
  error?: string;
  helpText?: string;
}>(), {
  id: undefined,
  label: "",
  placeholder: "선택",
  disabled: false,
  error: "",
  helpText: "",
});
</script>

<template>
  <label class="portal-field">
    <span v-if="label" class="portal-field-label">{{ label }}</span>
    <select :id="id" v-model="model" class="portal-control portal-select" :disabled="disabled">
      <option value="" disabled>{{ placeholder }}</option>
      <option v-for="option in options" :key="option.value" :value="option.value" :disabled="option.disabled">
        {{ option.label }}
      </option>
    </select>
    <span v-if="error" class="portal-field-error">{{ error }}</span>
    <span v-else-if="helpText" class="portal-field-help">{{ helpText }}</span>
  </label>
</template>
