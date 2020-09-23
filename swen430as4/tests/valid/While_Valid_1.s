
	.text
wl_reverse:
	pushq %rbp
	movq %rsp, %rbp
	subq $32, %rsp
	movq %rax, -16(%rbp)
	movq $0, %rax
	movq %rax, -24(%rbp)
	movq 24(%rbp), %rax
	movq %rax, -8(%rbp)
	movq -8(%rbp), %rax
	movq %rax, 16(%rbp)
	jmp label729
label729:
	movq %rbp, %rsp
	popq %rbp
	ret
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	call wl_reverse
	addq $16, %rsp
	movq -16(%rsp), %rax
	jmp label731
	movq $1, %rax
	jmp label732
label731:
	movq $0, %rax
label732:
	movq %rax, %rdi
	call assertion
label730:
	movq %rbp, %rsp
	popq %rbp
	ret
	.globl main
main:
	pushq %rbp
	call wl_main
	popq %rbp
	movq $0, %rax
	ret

	.data
