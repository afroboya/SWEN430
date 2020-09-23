
	.text
wl_f:
	pushq %rbp
	movq %rsp, %rbp
	movq 32(%rbp), %rax
	movq 24(%rbp), %rbx
	cmpq %rax, %rbx
	jnz label611
	movq $24, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $2, %rbx
	movq %rbx, 0(%rax)
	movq %rax, 16(%rbp)
	movq $69, %rbx
	movq %rbx, 8(%rax)
	movq $81, %rbx
	movq %rbx, 16(%rax)
	jmp label609
	jmp label610
label611:
	movq $56, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $6, %rbx
	movq %rbx, 0(%rax)
	movq %rax, 16(%rbp)
	movq $78, %rbx
	movq %rbx, 8(%rax)
	movq $79, %rbx
	movq %rbx, 16(%rax)
	movq $84, %rbx
	movq %rbx, 24(%rax)
	movq $32, %rbx
	movq %rbx, 32(%rax)
	movq $69, %rbx
	movq %rbx, 40(%rax)
	movq $81, %rbx
	movq %rbx, 48(%rax)
	jmp label609
label610:
label609:
	movq %rbp, %rsp
	popq %rbp
	ret
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	subq $32, %rsp
	movq $4, %rax
	movq %rax, 8(%rsp)
	movq $1, %rax
	movq %rax, 16(%rsp)
	call wl_f
	addq $32, %rsp
	movq -32(%rsp), %rax
	movq $56, %rbx
	subq $16, %rsp
	movq %rax, 0(%rsp)
	movq %rbx, %rdi
	call malloc
	movq %rax, %rbx
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq $6, %rcx
	movq %rcx, 0(%rbx)
	movq $78, %rcx
	movq %rcx, 8(%rbx)
	movq $79, %rcx
	movq %rcx, 16(%rbx)
	movq $84, %rcx
	movq %rcx, 24(%rbx)
	movq $32, %rcx
	movq %rcx, 32(%rbx)
	movq $69, %rcx
	movq %rcx, 40(%rbx)
	movq $81, %rcx
	movq %rcx, 48(%rbx)
	jmp label613
	movq $1, %rax
	jmp label614
label613:
	movq $0, %rax
label614:
	movq %rax, %rdi
	call assertion
	subq $32, %rsp
	movq $4, %rax
	movq %rax, 8(%rsp)
	movq $1, %rax
	movq %rax, 16(%rsp)
	call wl_f
	addq $32, %rsp
	movq -32(%rsp), %rax
	movq $56, %rbx
	subq $16, %rsp
	movq %rax, 0(%rsp)
	movq %rbx, %rdi
	call malloc
	movq %rax, %rbx
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq $6, %rcx
	movq %rcx, 0(%rbx)
	movq $78, %rcx
	movq %rcx, 8(%rbx)
	movq $79, %rcx
	movq %rcx, 16(%rbx)
	movq $84, %rcx
	movq %rcx, 24(%rbx)
	movq $32, %rcx
	movq %rcx, 32(%rbx)
	movq $69, %rcx
	movq %rcx, 40(%rbx)
	movq $81, %rcx
	movq %rcx, 48(%rbx)
	jmp label615
	movq $1, %rax
	jmp label616
label615:
	movq $0, %rax
label616:
	movq %rax, %rdi
	call assertion
	subq $32, %rsp
	movq $0, %rax
	movq %rax, 8(%rsp)
	movq $0, %rax
	movq %rax, 16(%rsp)
	call wl_f
	addq $32, %rsp
	movq -32(%rsp), %rax
	jmp label617
	movq $1, %rax
	jmp label618
label617:
	movq $0, %rax
label618:
	movq %rax, %rdi
	call assertion
label612:
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
